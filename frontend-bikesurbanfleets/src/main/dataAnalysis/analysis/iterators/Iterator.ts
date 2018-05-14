import { Observable } from '../ObserverPattern';

export interface Iterator extends Observable {
 iterate(): Promise<void>;
}