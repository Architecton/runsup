#!/bin/bash
node ./bin/www &
cd ..
cd mongodb
./mongod_local
