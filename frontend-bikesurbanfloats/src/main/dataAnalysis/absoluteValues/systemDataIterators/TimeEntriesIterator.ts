import { HistoryReader } from '../../../util/HistoryReader';
import { HistoryIterator } from '../../HistoryIterator';
import { TimeEntry } from '../../systemDataTypes/SystemInternalData';
import { Observer, Observable } from '../ObserverPattern';

export class TimeEntriesIterator implements Observable {
    private observers: Array<Observer>;
    
    private constructor() {
        this.observers = new Array<Observer>();
    }
    
    public static async create(): Promise<TimeEntriesIterator> {
        return new TimeEntriesIterator();
    }
    
        public async calculateBikeRentalsAndReturns(path: string): Promise<void> {
        let it: HistoryIterator = await HistoryIterator.create(path);
        let timeEntry: TimeEntry = await it.nextTimeEntry();
       
        while(timeEntry !== undefined) {
            this.notify(timeEntry);
            timeEntry = await it.nextTimeEntry();
        }
    }
        
    public notify(timeEntry: TimeEntry): void {
        for(let observer of this.observers) {
            observer.update(timeEntry);
        }
    }
    
    public subbscribe(observer: Observer): void {
        this.observers.push(observer);
    }

}
