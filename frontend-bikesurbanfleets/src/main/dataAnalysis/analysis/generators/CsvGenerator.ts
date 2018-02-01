import { HistoryEntitiesJson } from "../../../../shared/history";
import { JsonObject } from "../../../../shared/util";
import { HistoryReader } from "../../../util";
import { Station, User } from "../../systemDataTypes/Entities";
import { RentalsAndReturnsPerStation } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { RentalsAndReturnsPerUser } from "../absoluteValues/rentalsAndReturns/RentalsAndReturnsPerUser";
import { ReservationsPerStation } from "../absoluteValues/reservations/ReservationsPerStation";
import { ReservationsPerUser } from "../absoluteValues/reservations/ReservationsPerUser";
import * as json2csv from 'json2csv';
import * as fs from 'fs';

export class CsvGenerator {
  private titles: Array<string>;
	private stationData: Array<JsonObject>;
	private userData: Array<JsonObject>;
  private path: string;
  
  public constructor(path: string) {
    this.titles = new Array();
    this.stationData = new Array();
    this.userData = new Array();
    this.path = path;
  }

	public async generate(data: Map<string, any>): Promise<void> {
  await this.init(data);
		this.transformToCsv();
   return;
	}

	public async init(data: Map<string, any>): Promise<void> {
		this.titles.push('id');
		this.titles.push('bike_failed_reservations');
		this.titles.push('slot_failed_reservations');
		this.titles.push('bike_successful_reservations');
		this.titles.push('slot_successful_reservations');
		this.titles.push('bike_failed_rentals');
		this.titles.push('bike_failed_returns');
		this.titles.push('bike_successful_rentals');
		this.titles.push('bike_successful_returns');
    
    let history: HistoryReader = await HistoryReader.create(this.path);
    let entities: HistoryEntitiesJson = await history.getEntities('stations');    
    let stations: Array<Station> = <Station[]> entities.instances;
    entities = await history.getEntities('users');
    let users: Array<User> = <User[]> entities.instances;
    
    let stationJson: JsonObject = {};
    let userJson: JsonObject = {};
    let reservations, rentalsAndReturns: any;
    let value: number;
    
    reservations = data.get(ReservationsPerStation.name);
    rentalsAndReturns = data.get(RentalsAndReturnsPerStation.name);
    //console.log('reservations per station', reservations);
    //console.log('rentals and returns per station:', rentalsAndReturns);    
    for (let station of stations) {
      stationJson.id = station.id;
      value = reservations.getBikeFailedReservationsOfStation(station.id);
      stationJson.bike_failed_reservations = value;
      value = reservations.getSlotFailedReservationsOfStation(station.id);
      stationJson.slot_failed_reservations = value;
      value = reservations.getBikeSuccessfulReservationsOfStation(station.id);
      stationJson.bike_successful_reservations = value;
      value = reservations.getSlotSuccessfulReservationsOfStation(station.id);
      stationJson.slot_successful_reservations = value;
      
      value = rentalsAndReturns.getBikeFailedRentalsOfStation(station.id);
      stationJson.bike_failed_rentals = value;
      value = rentalsAndReturns.getBikeFailedReturnsOfStation(station.id);
      stationJson.bike_failed_returns = value;
      value = rentalsAndReturns.getBikeSuccessfulRentalsOfStation(station.id);
      stationJson.bike_successful_rentals = value;
      value = rentalsAndReturns.getBikeSuccessfulReturnsOfStation(station.id);
      stationJson.bike_successful_returns = value;
            
      this.stationData.push(stationJson);
    }
    
    reservations = data.get(ReservationsPerUser.name);
    rentalsAndReturns = data.get(RentalsAndReturnsPerUser.name);
    //console.log('reservations per user');
    //reservations.print();
    //console.log('rentals and returns per user:', rentalsAndReturns);    
    for (let user of users) {
      userJson.id = user.id;
      value = reservations.getBikeFailedReservationsOfUser(user.id);
      userJson.bike_failed_reservation = value;
      value = reservations.getSlotFailedReservationsOfUser(user.id);
      userJson.slot_failed_reservation = value;
      value = reservations.getBikeSuccessfulReservationsOfUser(user.id);
      userJson.bike_successful_reservation = value;
      value = reservations.getSlotSuccessfulReservationsOfUser(user.id);
      userJson.slot_successful_reservation = value;
      
      value = rentalsAndReturns.getBikeFailedRentalsOfUser(user.id);
      userJson.bike_failed_rentals = value;
      value = rentalsAndReturns.getBikeFailedReturnsOfUser(user.id);
      userJson.bike_failed_returns = value;
      value = rentalsAndReturns.getBikeSuccessfulRentalsOfUser(user.id);
      userJson.bike_successful_rentals = value;
      value = rentalsAndReturns.getBikeSuccessfulReturnsOfUser(user.id);
      userJson.bike_successful_returns = value;
                  
      this.userData.push(userJson);
    }
    return;
	}

	public transformToCsv(): void {
    let csv = json2csv({ data: this.stationData, fields: this.titles });
    fs.writeFile ('stations.csv', csv, (err) => {
      if (err) throw err;
      console.log('stations file saved');
    });
    csv = json2csv({ data: this.userData, fields: this.titles });
    fs.writeFile ('users.csv', csv, (err) => {
      if (err) throw err;
      console.log('user file saved');
    });    
	}

}  