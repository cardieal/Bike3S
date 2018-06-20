import * as paths from 'path';
import * as Ajv from 'ajv';
import * as fs from 'fs-extra';
import { spawn } from 'child_process';
import { app } from 'electron';
import { IpcUtil } from './index';
import {Main} from "../main";
import {CoreSimulatorArgs, UserGeneratorArgs} from "../../shared/BackendInterfaces";
import Channel from './Channel';

interface ValidationInfo {
    result: boolean;
    errors: string;
}


export default class BackendController {

    /*
    *   =================
    *   Channels        |
    *   =================
    */
    private static channels: Channel[] = [];
    private readonly window: Electron.BrowserWindow | null;

    /*
    *   =================
    *   Schemas         |
    *   =================
    */

    private globalSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/global-config.json'));
    private stationsSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/stations-config.json'));
    private entryPointSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/entrypoints-config.json'));
    private usersConfigSchema = fs.readJsonSync(paths.join(app.getAppPath(), 'schema/users-config.json'));

    /*
    *   =================
    *   json-validator  |
    *   =================
    */

    private jsonSchemaValidator = paths.join(app.getAppPath(), 'jsonschema-validator/jsonschema-validator.js');

    public static async create(): Promise<BackendController> {
        return new BackendController();
    }

    public static enableIpc(): void {
        IpcUtil.openChannel('backend-call-init', async () => {
            const backendCalls = await BackendController.create();

            this.channels = [
                new Channel('backend-call-generate-users', async (args: UserGeneratorArgs) => await backendCalls.generateUsers(args)),
                new Channel('backend-call-core-simulation', async (args: CoreSimulatorArgs) => await backendCalls.simulate(args))
            ];

            this.channels.forEach((channel) => IpcUtil.openChannel(channel.name, channel.callback));

            IpcUtil.openChannel('backend-call-close', async () => {
                IpcUtil.closeChannels('backend-call-close', ...this.channels.map((channel) => channel.name));
                this.enableIpc();
            });

            IpcUtil.closeChannel('backend-call-init');
        });
    }

    public static stopIpc(): void {
        IpcUtil.closeChannels('backend-call-close', ...this.channels.map((channel) => channel.name));
        this.enableIpc();
    }

    private constructor() {
        this.window = Main.simulate;
    }

    private validateConfiguration(schemaFile: string, configurationFile: string): ValidationInfo {
        let ajv = new Ajv({$data: true});
        console.log(schemaFile);
        console.log(configurationFile);
        let valid = ajv.validate(schemaFile, configurationFile);
        console.log(valid);

        if(valid) {
            return {result: true, errors: ajv.errorsText()};
        }
        return {result: false, errors: ajv.errorsText()};
    }

    private sendInfoToGui(channel: string, message: string): void {
        if(this.window) {
            console.log(message.toString());
            this.window.webContents.send(channel, message);
        }
    }

    public generateUsers(args: UserGeneratorArgs): Promise<void>{
        return new Promise((resolve: any, reject: any) => {

            let rootPath = app.getAppPath();
            console.log(rootPath);

            let entryPointsConf = fs.readJsonSync(args.entryPointsConfPath);

            // Entry Point Validation
            let entryPointsValidation = this.validateConfiguration(this.entryPointSchema, entryPointsConf);
            if(!entryPointsValidation.result) {
                this.sendInfoToGui('user-gen-error', entryPointsValidation.errors);
                reject("Error validating Entry Points: " + entryPointsValidation.errors);
            }

            let globalConf = fs.readJsonSync(args.globalConfPath);

            //Global Configuration Validation
            let globalValidation = this.validateConfiguration(this.globalSchema, globalConf);
            if(!globalValidation.result) {
                this.sendInfoToGui('user-gen-error', globalValidation.errors);
                reject("Error validating Global Configuration" + globalValidation.errors);
            }

            let command = 'java ' +
                '-jar ' +
                'bikesurbanfleets-config-usersgenerator-1.0.jar ' +
                '-entryPointsInput ' + '"' + args.entryPointsConfPath + '" ' +
                '-globalInput ' + '"' + args.globalConfPath  + '" ' +
                '-output ' + '"' + args.outputUsersPath + '/users-configuration.json" ' +
                '-callFromFrontend';


            const userGen = spawn(command, [], {
                cwd: rootPath,
                shell: true
            });

            userGen.stderr.on('data', (data) => {
                this.sendInfoToGui('user-gen-error', data.toString());
            });

            userGen.stdout.on('data', (data) => {
                this.sendInfoToGui('user-gen-data', data.toString());
            });

            userGen.on('close', (code) => {
                if (code === 0) {
                    console.log('User generation finished');
                    resolve();
                }
                else {
                    reject("Fail executing bikesurbanfleets-config-usersgenerator-1.0.jar");
                }
            });
        });
    }

    public simulate(args: CoreSimulatorArgs): Promise<void> {
        return new Promise(async (resolve: any, reject: any) => {
            let rootPath = app.getAppPath();
            console.log(rootPath);
            let globalConf, stationsConf, usersConf: any;
            try {
                globalConf = await fs.readJson(args.globalConfPath);
                stationsConf = await fs.readJson(args.stationsConfPath);
                usersConf = await fs.readJsonSync(args.usersConfPath);
            }
            catch {
                let errorMessage = "Error reading Configuration Path: \n"
                    + "Global Configuration: " + args.globalConfPath + "\n"
                    + "Users Configuration: " + args.usersConfPath + "\n"
                    + "Stations Configuration: " + args.stationsConfPath + "\n";
                this.sendInfoToGui('core-error', errorMessage);
                reject(errorMessage);
            }

            //Global Configuration Validation
            console.log(this.globalSchema);
            console.log(globalConf);
            let globalValidation = this.validateConfiguration(this.globalSchema , globalConf);
            if(!globalValidation.result) {
                this.sendInfoToGui('core-error', globalValidation.errors);
                reject("Error validating Global Configuration" + globalValidation.errors);
            }

            //Stations Configuration Validation
            console.log(this.stationsSchema);
            console.log(stationsConf);
            let stationsValidation = this.validateConfiguration(this.stationsSchema, stationsConf);
            console.log(stationsValidation);
            if(!stationsValidation.result) {
              this.sendInfoToGui('core-error', stationsValidation.errors);
              reject("Error validating stations" + stationsValidation.errors);
            }

            //User generation validation
            console.log(this.usersConfigSchema);
            console.log(usersConf);
            let usersValidation = this.validateConfiguration(this.usersConfigSchema, usersConf);
            console.log(usersValidation);
            if(!usersValidation.result) {
                this.sendInfoToGui('core-error', usersValidation.errors);
                reject("Error validating users", + usersValidation.errors);
            }

            // Command should be in an unique string with quotation marks to make it compatible with MacOs 
            let command = "java " +
                "-DLogFilePath=${HOME}/.Bike3S/ " + 
                "-jar " +
                "bikesurbanfleets-core-1.0.jar " +
                "-globalConfig " + '"' + args.globalConfPath + '" ' +
                "-usersConfig " + '"' + args.usersConfPath + '" ' +
                "-stationsConfig " + '"' + args.stationsConfPath + '" ' + 
                "-historyOutput " + '"' + args.outputHistoryPath + '" '+ 
                "-callFromFrontend";

            const sim = spawn(command, [], {
                cwd: rootPath,
                shell: true
            });

            sim.stderr.on('data', (error) => {
                console.log(error);
                this.sendInfoToGui('core-error', error.toString());
            });

            sim.stdout.on('data', (data) => {
                console.log(data);
                this.sendInfoToGui('core-data', data.toString());
            });

            sim.on('close', (code) => {
                if (code === 0) {
                    console.log("Simulation Finished");
                    resolve();
                }
                else {
                    reject("Fail executing bikesurbanfleets-core-1.0.jar");
                }
            });
        });
    }


}