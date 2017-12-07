import { isPlainObject } from 'lodash';

interface PlainObject {
    [key: string]: any,
    [key: number]: any,
}

interface JsonObject {
    [key: string]: JsonValue
}

interface JsonArray extends Array<JsonValue> {}

type JsonValue = null | string | number | boolean | JsonArray | JsonObject;

namespace Tree {

    interface TreeNode {
        parent: PlainObject,
        name: string,
    }

    function fetch(tree: PlainObject, path: string): TreeNode {
        const names = path.split('.');
        let parent = tree;

        names.slice(0, -1).forEach((key) => parent = parent[key]);

        return {
            parent: parent,
            name: names.slice(-1)[0],
        };
    }

    function traverseInternal(tree: PlainObject, atLeaf: LeafCallback, path: Array<string>): PlainObject {
        Object.keys(tree).forEach((key) => {
            if (isPlainObject(tree[key])) {
                traverseInternal(tree[key], atLeaf, [...path, key]);
            } else {
                atLeaf(tree, key, [...path, key]);
            }
        });
        return tree;
    }

    export type LeafCallback = (parent: PlainObject, key: string, path: Array<string>) => void;

    export function get(tree: PlainObject, path: string): any {
        const node = fetch(tree, path);
        return node.parent[node.name];
    }

    export function set(tree: PlainObject, path: string, value: any) {
        const node = fetch(tree, path);
        node.parent[node.name] = value;
    }

    export function traverse(tree: PlainObject, atLeaf: LeafCallback): PlainObject {
        return traverseInternal(tree, atLeaf, []);
    }
}

export {
    PlainObject,
    JsonValue,
    JsonObject,
    JsonArray,
    Tree,
}
