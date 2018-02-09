import { HistoryEntitiesJson } from "../../../shared/history";
import { HistoryReader } from "../../util";
import { User } from "../systemDataTypes/Entities";
import { RentalsAndReturnsPerStation } from "./absoluteValues/rentalsAndReturns/RentalsAndReturnsPerStation";
import { ReservationsPerStation } from "./absoluteValues/reservations/ReservationsPerStation";

export class SystemGlobalValues {
  private numberUsers: number;
  private data: Map<string, any>;
  
  private totalRentals: number;
  private totalReturns: number; 
  private totalBikeReservations: number;
  private totalSlotReservations: number;
  
  private successfulRentals: number;
  private successfulReturns: number;
  private successfulBikeReservations: number;
  private successfulSlotReservations: number;  
  
  private failedRentals: number;
  private failedReturns: number;
  private failedBikeReservations: number;
  private failedSlotReservations: number;
  
  private demandSatisfaction: number;
  private rentalEfficiency: number;
  
  public constructor(data) {
    this.data = data;
    this.totalRentals = 0;
    this.totalReturns = 0; 
    this.totalBikeReservations = 0;
    this.totalSlotReservations = 0;
  
    this.successfulRentals = 0;
    this.successfulReturns = 0;
    this.successfulBikeReservations = 0;
    this.successfulSlotReservations = 0;  
  
    this.failedRentals = 0;
    this.failedReturns = 0;
    this.failedBikeReservations = 0;
    this.failedSlotReservations = 0;
  
    this.demandSatisfaction = 0;
  }
  
  public async init(path): Promise<void> {
    let history: HistoryReader = await HistoryReader.create(path);
    let entities: HistoryEntitiesJson = await history.getEntities("users");
    let users: Array<User> = entities.instances;
    this.numberUsers = users.length;
  
  }
  
  
  public calculateGlobalData(): void {
    let reservations: ReservationsPerStation = this.data.get(ReservationsPerStation.name);
    reservations.getBikeSuccessfulReservations().forEach( (v, k) => this.successfulBikeReservations += v); 
    reservations.getSlotSuccessfulReservations().forEach( (v, k) => this.successfulSlotReservations += v);
    reservations.getBikeFailedReservations().forEach( (v, k) => this.failedBikeReservations += v);
    reservations.getSlotFailedReservations().forEach( (v, k) => this.failedSlotReservations += v);
    
    this.totalBikeReservations = this.successfulBikeReservations + this.failedBikeReservations;
    this.totalSlotReservations = this.successfulSlotReservations + this.failedSlotReservations;
      
    let rentalsAndReturns: RentalsAndReturnsPerStation = this.data.get(RentalsAndReturnsPerStation.name);
    rentalsAndReturns.getBikeSuccessfulRentals().forEach( (v, k) => this.successfulRentals += v);
    rentalsAndReturns.getBikeSuccessfulReturns().forEach( (v, k) => this.successfulReturns += v);
    rentalsAndReturns.getBikeFailedRentals().forEach( (v, k) => this.failedRentals += v);
    rentalsAndReturns.getBikeFailedReturns().forEach( (v, k) => this.failedReturns += v);
    
    this.totalRentals = this.successfulRentals + this.failedRentals;
    this.totalReturns = this.successfulReturns + this.failedReturns;
    
    this.demandSatisfaction = this.successfulRentals / this.numberUsers;  
    this.rentalEfficiency = this.successfulRentals / this.totalRentals;
  
  }
  
  
   
}