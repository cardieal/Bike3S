import { ipcMain, Event } from 'electron';

export default class IpcUtil {

    static openChannel(channel: string, onSuccess: (data?: any) => Promise<any>, onError?: (error?: Error) => void): void {
        ipcMain.on(channel, async (event: Event, data?: any) => {
            try {
                event.sender.send(channel, {
                    status: 200,
                    data: await onSuccess(data)
                });
            } catch (error) {
                event.sender.send(channel, {
                    status: 500,
                    data: error
                });
                onError && onError(error);
            }
        });
    }

    static closeChannel(channel: string): void {
        ipcMain.removeAllListeners(channel);
    }

    static closeChannels(...channels: Array<string>): void {
        channels.forEach(this.closeChannel);
    }

}
