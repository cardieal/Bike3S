import  { Station, User } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { Observer } from '../../ObserverPattern';
import { RentalsAndReturnsInfo } from './RentalsAndReturnsInfo';
import { Info } from '../Info';

export class RentalsAndReturnsPerStation implements Observer, Info {
    private stations: Array<Station>;
    private rentalsAndReturns: RentalsAndReturnsInfo;
    
    public constructor(stations: Array<Station>) {
        this.stations = stations;
        this.rentalsAndReturns = new RentalsAndReturnsInfo();
    }
    
    public async init() {
        try {
            await this.rentalsAndReturns.initData(this.stations);
        }
        catch(error) {
            throw new Error('Error initializing data: '+error);
        }
        return;
    }
    
    public static async create(stations: Array<Station>) {
        let stationValues = new RentalsAndReturnsPerStation(stations);
        try {
            await stationValues.init();
        }
        catch(error) {
            throw new Error('Error creating requested data: '+error);
        }
        return stationValues;
    }
  
    public update(timeEntry: TimeEntry): void {
        let events: Array<Event> = timeEntry.events;
        let key: number | undefined;
        let stations: Array<Station>;
        
        for (let event of events) {
            stations = event.changes.stations;
            
            switch(event.name) {
                case 'EventUserArrivesAtStationToRentBikeWithReservation': { 
                    key = this.obtainChangedStationId(stations);
                    if (key !== undefined) {  // it's sure key isn't undefined because the user has a bike reservation  
                        this.rentalsAndReturns.increaseSuccessfulRentals(key);
                    }
                    break;
                }
            
                case 'EventUserArrivesAtStationToReturnBikeWithReservation': {
                    key = this.obtainChangedStationId(stations);
                    if (key !== undefined) {  // it's sure key isn't undefined because the user has a bike reservation
                        this.rentalsAndReturns.increaseSuccessfulReturns(key);
                    }
                    break;
                }
                
                case 'EventUserArrivesAtStationToRentBikeWithoutReservation': {
                    if (stations.length > 0) {
                        // If only stations with reservations have been recorded, key'll be undefined 
                        key = this.obtainChangedStationId(stations);
                        // If key is undefined, successful rentals won't be increased
                        if (key  !== undefined) { 
                            this.rentalsAndReturns.increaseSuccessfulRentals(key);
                        }
                    }
                    
                    // If there're not registered stations, it means rental hasn't been possible
                    else {
                        key = this.obtainNotChangedStationId(event.changes.users[0]);
                        this.rentalsAndReturns.increaseFailedRentals(key);
                    }
                    break;
                }
            
                case 'EventUserArrivesAtStationToReturnBikeWithoutReservation': {
                    if (stations.length > 0) {
                        // If only stations with reservations have been recorded, key'll be undefined
                        key = this.obtainChangedStationId(stations);
                        // If key is undefined, successful returns won't be increased
                        if (key !== undefined) {
                            this.rentalsAndReturns.increaseSuccessfulReturns(key);
                        }
                    }
                    
                    // If there're not registered stations, it means rental hasn't been possible
                    else {
                        key = this.obtainNotChangedStationId(event.changes.users[0]);
                        this.rentalsAndReturns.increaseFailedReturns(key);
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * It finds out the station id from the last point of the route travelled by a user,
     * looking for which station is at that point.       
     */
    private obtainNotChangedStationId(user: User): number {
        let lastPos: number = user.route.old.points.length-1;
        let stationPosition: any = user.route.old.points[lastPos];
         
        let stationId: number = -1;
        for(let station of this.stations) {
            if (station.position.latitude === stationPosition.latitude && station.position.longitude === stationPosition.longitude) {
                stationId = station.id;
                break; 
            }
        }
        return stationId;
    }
    
    /**
     * It finds out the station id lokking for a change registered on the station 
     * bikes; if only changes in reservations have been registered, it returns undefined    
     */
    private obtainChangedStationId(stations: Array<Station>): number | undefined {
        for (let station of stations) {
            if (station.bikes !== undefined) {
                return station.id;
            }
        }
        return undefined;
    }
    
    public getRentalsAndReturns(): RentalsAndReturnsInfo {
        return this.rentalsAndReturns;
    }

}
