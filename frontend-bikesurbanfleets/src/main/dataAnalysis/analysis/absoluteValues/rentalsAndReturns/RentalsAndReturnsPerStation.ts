import { HistoryReader } from '../../../../util';
import { HistoryEntitiesJson } from '../../../../../shared/history';
import { HistoryIterator } from "../../../HistoryIterator";
import  { Station, User } from '../../../systemDataTypes/Entities';
import  { TimeEntry, Event } from '../../../systemDataTypes/SystemInternalData';
import { RentalsAndReturnsInfo } from './RentalsAndReturnsInfo';

export class RentalsAndReturnsPerStation extends RentalsAndReturnsInfo {
    private stations: Array<Station>;
    
    public constructor() {
        super('STATION');
    }
    
    public async init(path: string): Promise<void> {
        try {
            let history: HistoryReader = await HistoryReader.create(path);
            let entities: HistoryEntitiesJson = await history.getEntities("stations");
            this.stations = <Station[]> entities.instances;
                
            await this.initData(this.stations);
        }
        catch(error) {
            throw new Error('Error accessing to stations: '+error);
        }
        return;
    }
    
    public static async create(path: string): Promise<RentalsAndReturnsPerStation> {
        let stationValues = new RentalsAndReturnsPerStation();
        try {
            await stationValues.init(path);
        }
        catch(error) {
            throw new Error('Error initializing station data of the requested data: '+error);
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
                    this.increaseSuccessfulRentals(key);
                    break;
                }
            
                case 'EventUserArrivesAtStationToReturnBikeWithReservation': {
                    key = this.obtainChangedStationId(stations);
                    this.increaseSuccessfulReturns(key);
                    break;
                }
                
                case 'EventUserArrivesAtStationToRentBikeWithoutReservation': {
                    if (stations.length > 0) {
                        // If only stations with reservations have been recorded, key'll be undefined 
                        key = this.obtainChangedStationId(stations);
                        // If key is undefined, successful rentals won't be increased 
                        this.increaseSuccessfulRentals(key);
                    }
                    
                    // If there're not registered stations, it means rental hasn't been possible
                    else {
                        key = this.obtainNotChangedStationId(event.changes.users[0]);
                        this.increaseFailedRentals(key);
                    }
                    break;
                }
            
                case 'EventUserArrivesAtStationToReturnBikeWithoutReservation': {
                    if (stations.length > 0) {
                        // If only stations with reservations have been recorded, key'll be undefined
                        key = this.obtainChangedStationId(stations);
                        // If key is undefined, successful rentals won't be increased
                        this.increaseSuccessfulReturns(key);
                    }
                    
                    // If there're not registered stations, it means rental hasn't been possible
                    else {
                        key = this.obtainNotChangedStationId(event.changes.users[0]);
                        this.increaseValue(this.bikeFailedReturnsPerStation, key);
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


}
