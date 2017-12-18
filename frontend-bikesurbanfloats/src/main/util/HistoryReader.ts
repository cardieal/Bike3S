import * as AJV from 'ajv';
import * as fs from 'fs-extra';
import * as paths from 'path';

import { app } from 'electron';
import { without } from 'lodash';

import { HistoryEntities, HistoryTimeEntries } from '../../shared/history';
import { IpcChannel, IpcUtil } from './index';

interface TimeRange {
    start: number,
    end: number
}

export default class HistoryReader {

    private static ajv = new AJV({
        $data: true,
        // allErrors: true,
        verbose: true,
    });

    private static entityFileSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/entities.json'))
    private static changeFileSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/timeentries.json'));

    private historyPath: string;
    private changeFiles: Array<string>;
    private currentIndex: number;

    static async create(path: string): Promise<HistoryReader> {
        let reader = new HistoryReader(path);
        reader.changeFiles = without(await fs.readdir(reader.historyPath), 'entities.json');
        return reader;
    }

    static enableIpc(): void {
        IpcUtil.openChannel('history-init', async (historyPath: string) => {
            const reader = await this.create(historyPath);

            const channels = [
                new IpcChannel('history-entities', async () => await reader.readEntities()),
                new IpcChannel('history-previous', async () => await reader.previousChangeFile()),
                new IpcChannel('history-next', async () => await reader.nextChangeFile()),
                new IpcChannel('history-nchanges', async () => reader.numberOfChangeFiles),
                new IpcChannel('history-range', async () => reader.timeRange),
            ];

            channels.forEach((channel) => IpcUtil.openChannel(channel.name, channel.callback));

            IpcUtil.openChannel('history-close', async () => {
                IpcUtil.closeChannels('history-close', ...channels.map((channel) => channel.name));
                this.enableIpc();
            });

            IpcUtil.closeChannel('history-init');
        });
    }

    private constructor(path: string) {
        this.currentIndex = -1;
        this.historyPath = paths.join(app.getAppPath(), path);
    }

    clipToRange(start: number, end: number = this.timeRange.end): void {
        if (this.currentIndex !== -1) {
            throw new Error(`Clipping is only allowed before reading any change file!`);
        }

        if (start < this.timeRange.start) {
            throw new Error(`start may not be lower than current range`);
        }

        if (end > this.timeRange.end) {
            throw new Error(`end may not be higher than current range`);
        }

        this.changeFiles = this.changeFiles.filter((file) => {
            const [fileStart, fileEnd] = file.split('_')[0].split('-').map(parseInt);
            return end >= fileStart && start <= fileEnd;
        });
    }

    async readEntities(): Promise<HistoryEntities> {
        const entities = await fs.readJson(paths.join(this.historyPath, 'entities.json'));

        if (!HistoryReader.ajv.validate(HistoryReader.entityFileSchema, entities)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return entities;
    }

    async previousChangeFile(): Promise<HistoryTimeEntries> {
        if (this.currentIndex <= 0) {
            throw new Error(`No previous change file available!`);
        }

        const file = await fs.readJson(paths.join(this.historyPath, this.changeFiles[--this.currentIndex]));

        if (!HistoryReader.ajv.validate(HistoryReader.changeFileSchema, file)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return file;
    }

    async nextChangeFile(): Promise<HistoryTimeEntries> {
        if (this.currentIndex === this.changeFiles.length - 1) {
            throw new Error(`No next change file available!`);
        }

        const file = await fs.readJson(paths.join(this.historyPath, this.changeFiles[++this.currentIndex]));

        if (!HistoryReader.ajv.validate(HistoryReader.changeFileSchema, file)) {
            throw new Error(HistoryReader.ajv.errorsText());
        }

        return file;
    }

    get numberOfChangeFiles(): number {
        return this.changeFiles.length;
    }

    get timeRange(): TimeRange {
        const range = [
            this.changeFiles[0],
            this.changeFiles.slice(-1)[0]
        ].map((file, index) => parseInt(file.split('_')[0].split('-')[index]));

        return {
            start: range[0],
            end: range[1]
        }
    }
}
