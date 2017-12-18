import { BuilderOptions, SchemaVersion } from 'json-schema-builder-ts';
import GeoPoint from './geopoint';
import idReference from './idreference';
import ReservationState from './reservationstate';
import ReservationType from './reservationtype';
import Route from './route';
import UInt from './uint';
import UserType from './usertype';

const options: BuilderOptions = {
    version: SchemaVersion.DRAFT_6,
    allowDataReference: true,
};

export {
    options,
    idReference,
    GeoPoint,
    ReservationState,
    ReservationType,
    Route,
    UInt,
    UserType
}
