import {Component, Inject} from "@angular/core";
import {CoreSimulatorArgs, UserGeneratorArgs} from "../../../shared/BackendInterfaces";
import * as $ from "jquery";
import {AjaxProtocol} from "../../ajax/AjaxProtocol";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
const { dialog } = (window as any).require('electron').remote;
const { ipcRenderer } = (window as any).require('electron');

@Component({
    selector: 'simulatecore-component',
    template: require('./simulatecore.component.html'),
    styles: [require('./simulatecore.component.css')]
})
export class SimulatecoreComponent{

    private globalConfiguration: string;
    private usersConfiguration: string;
    private stationsConfiguration: string;
    private historyOutputPath: string;

    private resultMessage: string;
    private exceptions: string;
    private stdout: string;
    private errors: boolean;

    constructor(@Inject('AjaxProtocol') private ajax: AjaxProtocol, private modalService: NgbModal) {}

    async ngOnInit() {
        this.resultMessage = "";
        this.exceptions = "";

        ipcRenderer.on('core-error' , (event: Event, data: string) => this.addErrors(data));
        ipcRenderer.on('core-data', (event: Event, data: string) => this.addConsoleMessage(data));

        await this.ajax.backend.init();
    }

    selectFile(): string {
        return dialog.showOpenDialog({
            properties: ['openFile'],
            filters: [{name: 'JSON Files', extensions: ['json']}]
        })[0];
    }

    selectFolder(): string {
        return dialog.showOpenDialog({properties: ['openDirectory']})[0];
    }

    open(content: any) {
        this.modalService.open(content).result.then(() => {
        });
    }

    addErrors(error: string) {
        this.errors = true;
        this.resultMessage = "Simulation has ended with exceptions";
        this.exceptions += error;
    }

    addConsoleMessage(message: string) {
        this.stdout += message;
    }

    async runSimulation() {
        this.errors = false;
        this.resultMessage = "Simulation in progress...";
        this.exceptions = "";
        this.stdout = "";
        let args: CoreSimulatorArgs = {
            globalConf: this.globalConfiguration,
            usersConf: this.usersConfiguration,
            stationsConf: this.stationsConfiguration,
            outputHistory: this.historyOutputPath
        };
        $('#modal-button').trigger('click');
        await this.ajax.backend.simulate(args);
        if(!this.errors) {
            this.resultMessage = "Simulation completed";
        }
    }

}