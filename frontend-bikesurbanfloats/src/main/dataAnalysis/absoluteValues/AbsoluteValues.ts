import { HistoryReader } from '../../util/HistoryReader';
import { HistoryIterator } from '../HistoryIterator';
import { HistoryEntities, HistoryEntitiesJson } from '../../../shared/History' 
import { Reservation } from '../dataTypes/Entities';
import { ReservationsAbsoluteValues } from './users/ReservationsAbsoluteValues';
import { Observer, Observable } from './ObserverPattern';

export class AbsoluteValues implements Observable {
    private path: string;
    private observers: Array<Observer>;
    
    // TODO: doess an observed object provide a subscribe method? 
    public subbscribe(observer: Observer): void {
        this.observers.push(observer);
    }
    
    public async calculateReservations(): Promise<void> {
        let history: HistoryReader = await HistoryReader.create(this.path);
        let reservations: Array<Reservation> = await history.getEntities("reservations").instances;
        
        for (let reservation of reservations) {
            this.notify(reservation);
        }
    }
    
    public async calculateBikeRentalsAndReturns(): Promise<void> {
        let it: HistoryIterator = await HistoryIterator.create(this.path);
        let timeEntry: any = it.nextTimeEntry();
       
        while(timeEntry !== undefined) {
            this.notify(timeEntry);
            timeEntry = await it.nextTimeEntry();
        }
    }
    
    public notify(data: any): void {
        for(let observer of this.observers) {
            observer.update(data);
        }
        
    }
    
    public async calculateAbsoluteValues(): Promise<void> {
        await this.calculateReservations();
        await this.calculateBikeRentalsAndReturns();
    }
 
        
}