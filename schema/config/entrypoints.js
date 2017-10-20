const merge = require('../util/merge');

const GeoPoint = require('../common/geopoint');

const distributionBase = {
    type: 'object',
    additionalProperties: false,
    required: ['type']
};

const distributions = [
    {
        properties: {
            type: { const: 'random' }
        }
    },
    {
        required: ['lambda'],
        properties: {
            type: { const: 'poisson' },
            lambda: { type: 'number' }
        },
    }
].map((distribution) => merge(distribution, distributionBase));

const itemBase = {
    type: 'object',
    additionalProperties: false,
    required: ['userType', 'position'],
    properties: {
        userType: {
            enum: [
                'UserTest',
            ]
        },
        position: GeoPoint,
        radio: {
            type: 'integer',
            minimum: 0
        }
    },
};

const validItems = [
    {
        required: ['timeInstant'],
        properties: {
            timeInstant: {
                type: 'integer',
                minimum: 0
            }
        },
    },
    {
        required: ['distribution'],
        properties: {
            distribution: {
                oneOf: distributions,
            }
        }
    }
].map((item) => merge(item, itemBase));

module.exports = {
    $schema: 'http://json-schema.org/draft-06/schema#',
    type: 'array',
    items: {
        oneOf: validItems
    }
};