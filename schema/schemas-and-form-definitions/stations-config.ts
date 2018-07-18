import {GeoPoint, options, UInt} from "../common/index";
import {sArray, sInteger, sNull, sObject} from "json-schema-builder-ts/dist/types";
import {JsonSchema} from "json-schema-builder-ts";
import {rData} from "json-schema-builder-ts/dist/references";
import {sAnyOf} from "json-schema-builder-ts/dist/operators/schematical";

const Bike = sObject();

export const Station = sObject({
    position: GeoPoint,
    capacity: UInt,
    bikes: sAnyOf(
        sInteger().min(0).max(rData('1/capacity')),
        sArray(sAnyOf(Bike, sNull())).min(rData('1/capacity')).max(rData('1/capacity'))
    ),
}).require.all().restrict();

export const layout = 
[
    {key: "position.latitude", placeholder: "Latitude"},
    {key: "position.longitude", placeholder: "Longitude"},
    {key: "capacity", placeholder: "Capacity of the station"},
    {key: "bikes", placeholder: "Number of bikes at the station"}
]