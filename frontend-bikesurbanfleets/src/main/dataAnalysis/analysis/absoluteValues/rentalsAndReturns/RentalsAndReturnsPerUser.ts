import  { User } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { Observer } from "../../ObserverPattern";
import { Data } from "../Data";
import { SystemInfo } from "../SystemInfo";
import { RentalsAndReturnsData } from './RentalsAndReturnsData';

export class RentalsAndReturnsPerUser implements SystemInfo, Observer {
    basicData: Array<User>;
    data: RentalsAndReturnsData;
    
    public constructor(users: Array<User>) {
        this.basicData = users;
        this.data = new RentalsAndReturnsData();
    }
  
    public async init() {
        try {
            await this.data.initData(this.basicData);
        }
        catch(error) {
            throw new Error('Error initializing data: '+error);
        }
        return;
    }

    public static async create(users: Array<User>) {
        let rentalsAndReturnsValues = new RentalsAndReturnsPerUser(users);
        try {
            await rentalsAndReturnsValues.init();
        }
        catch(error) {
            throw new Error('Error creating requested data: '+error);
        }
        return rentalsAndReturnsValues;
    }
  
    public update(timeEntry: TimeEntry): void {
        let events: Array<Event> = timeEntry.events;
        let key: number;

        for(let event of events) {
            key = event.changes.users[0].id;
            
            switch(event.name) { 
                case 'EventUserArrivesAtStationToRentBikeWithReservation': {
                    this.data.increaseSuccessfulRentals(key);
                    break;
                }
                
                case 'EventUserArrivesAtStationToReturnBikeWithReservation': {
                    this.data.increaseSuccessfulReturns(key);
                    break;
                }
            
                case 'EventUserArrivesAtStationToRentBikeWithoutReservation': {
                    let bike: any = event.changes.users[0].bike;
                    if (bike !== undefined) {
                        this.data.increaseSuccessfulRentals(key);
                    }
                    else {
                      this.data.increaseFailedRentals(key);
                    }
                    break;
                }
    
                case 'EventUserArrivesAtStationToReturnBikeWithoutReservation': {
                    let bike: any = event.changes.users[0].bike;
                    if (bike !== undefined) {
                        this.data.increaseSuccessfulRentals(key);
                    }
                    else {
                      this.data.increaseFailedRentals(key);
                    }
                    break;
                }
            }
        }
    }

    public getData(): RentalsAndReturnsData {
        return this.data;
    }
      
}