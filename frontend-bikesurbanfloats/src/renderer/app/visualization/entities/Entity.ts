import { JsonObject } from '../../../../shared/util';
import { VisualizationComponent } from '../visualization.component';

export abstract class Entity {
    constructor(private $id: number) {}

    get id() {
        return this.$id;
    }
}

export interface VisualOptions {
    fromJson: string,
}

export const EntityMetaKey = Symbol('entity-meta-key');

export function VisualEntity<J extends JsonObject>(options: VisualOptions) {
    return function <E extends Entity> (Target: { new(json: J): E }) {
        Reflect.defineMetadata(EntityMetaKey, options, Target);
        return Target;
    }
}
