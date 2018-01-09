import { app, BrowserWindow, shell } from 'electron';
import { join } from 'path';
import { format as urlFormat } from 'url';
import { settingsPathGenerator } from '../shared/settings';
import { ReservationsPerUser } from "./dataAnalysis/absoluteValues/analysisData/users/ReservationsPerUser";
import { ReservationsIterator } from "./dataAnalysis/absoluteValues/systemDataIterators/ReservationsIterator";
import { Settings } from './settings';
import { HistoryReader } from './util';

namespace Main {
    let window: Electron.BrowserWindow | null;

    function createWindow() {
        window = new BrowserWindow({ width: 800, height: 600 });

        window.loadURL(urlFormat({
            pathname: join(app.getAppPath(), 'frontend', 'index.html'),
            protocol: 'file',
            slashes: true
        }));

        window.on('closed', () => window = null);

        window.webContents.on('will-navigate', (event, url) => {
            event.preventDefault(); // prevents dragging images or other documents into browser window
            shell.openExternal(url); // opens links (or dragged documents) in external browser
        });

        if (process.env.target === 'development') {
            window.webContents.openDevTools();
        }
    }

    export function init() {
        app.on('ready', async () => {
            HistoryReader.enableIpc();
            Settings.enableIpc();

            if (process.env.target === 'development') {
                const extensions = await Settings.get(settingsPathGenerator().development.extensions());
                Object.values(extensions).forEach(BrowserWindow.addDevToolsExtension);
            }

            createWindow();
        });

        app.on('window-all-closed', async () => {
            await Settings.write();
            if (process.platform !== 'darwin') app.quit();
        });

        app.on('activate', () => {
            if (window === null) createWindow();
        });

    }
    
    export async function ptm() {
        let values = await ReservationsPerUser.create('history');
        let it = await ReservationsIterator.create('history');
        it.subscribe(values);
        it.calculateReservations();
        console.log('user 1:  ', values.getBikeSuccessfulReservationsOfUser(1));
    } 
   
    
}
  
Main.init();
Main.ptm();

