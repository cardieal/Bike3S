const { SNumber, SObject, SArray, Schema } = require('../util/jsonschema');
const { XMin, RequireAll } = require('../util/jsonschema/constraints');
const { UInt, GeoPoint, UserType, ReservationState, ReservationType, IdReference } = require('../util/customtypes');

const User = SObject({
    id: UInt,
    type: UserType,
    walkingVelocity: SNumber(XMin(0)),
    cyclingVelocity: SNumber(XMin(0)),
}, RequireAll());

const Bike = SObject({
    id: UInt,
}, RequireAll());

const Station = SObject({
    id: UInt,
    position: GeoPoint,
    capacity: UInt,
    bikes: SArray(IdReference)
}, RequireAll());

const Reservation = SObject({
    id: UInt,
    startTime: UInt,
    endTime: UInt,
    user: UInt,
    station: UInt,
    bike: IdReference,
    type: ReservationType,
    state: ReservationState
}, RequireAll());

module.exports = Schema(SObject({
    users: SArray(User),
    bikes: SArray(Bike),
    stations: SArray(Station),
    reservations: SArray(Reservation)
}, RequireAll()));