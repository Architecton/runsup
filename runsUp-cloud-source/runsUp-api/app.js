
// All request to the Express server are initialy processed here.
var passport = require('passport');
require('./api/models/db');
require('./api/configuration/passport');
require('dotenv').load();
var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
var helmet = require('helmet')


// routers located in app_server directory
// var indexRouter = require('./app_server/routes/index');
var indexApi = require('./api/routes/index');

// app - the application
var app = express();

// Middleware to handle certain security flaws.
app.use(function(req, res, next) {
  res.setHeader('X-Frame-Options', 'DENY');
  res.setHeader('X-XSS-Protection', '1; mode=block');
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
  res.setHeader("Pragma", "no-cache"); // HTTP 1.0.
  res.setHeader("Expires", "0"); // Proxies.
  next();
});


// setup logger
app.use(logger('dev'));
// setup json functionality

/*
The json and urlencoded middleware are both part of bodyParser. This is what the README says:

bodyParser([options])
Returns middleware that parses both json and urlencoded. The options are passed to both middleware.

bodyParser.json([options])
Returns middleware that only parses json. The options are:

strict - only parse objects and arrays
limit <1mb> - maximum request body size
reviver - passed to JSON.parse()
bodyParser.urlencoded([options])
Returns middleware that only parses urlencoded with the qs module. The options are:

limit <1mb> - maximum request body size
*/

app.use(express.json());
app.use(express.urlencoded({ extended: false }));

// setup cookie parser
app.use(cookieParser());


// Use passport
app.use(passport.initialize());

// Add router -- Forward requests to indexRouter
// app.use('/', indexRouter);

// forward all request beginning with /api to indexAPI router
app.use(indexApi);


// catch 404 and forward to error handler.
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler for errors with 404 and 500 statuses
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

// Error handler for authentication errors
app.use(function(err, req, res, next) {
  if (err.name === 'UnauthorizedError') {
    res.status(401);
    res.json({
      "message": err.name + ": " + err.message
    });
  }
});

// Expose app as module.
module.exports = app;
