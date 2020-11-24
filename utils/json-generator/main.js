#!/usr/bin/env node

const faker = require('faker');
const fs = require('fs');
const args = require('args')

args
  .option('out', 'Output file', `${__dirname}/../../data/data.json`)
  .option('records', 'Amount of records', 409600)

const options = args.parse(process.argv)

var stream = fs.createWriteStream(options.out, { flags: 'a' });

const records = Number(options.records)
for (let i = 0; i < records; i++) {
  stream.write(JSON.stringify(faker.helpers.createCard()) + '\n');
}

stream.end();
