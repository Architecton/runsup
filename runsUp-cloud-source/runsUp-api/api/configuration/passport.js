var passport = require('passport');
var LocalStrategy = require('passport-local').Strategy;
var mongoose = require('mongoose');
var User = mongoose.model('User');

// Set local strategy
passport.use(new LocalStrategy({
        usernameField: 'accId',
        passwordField: 'accId'
    },
    function(accId, accId, done) {
        User.findById(accId).exec(
            function(error, user) {
                if (error) {
                    return done(error);
                }
                if (!user) {
                    return done(null, false, {
                        message: 'Authentication error'
                    });
                }
                return done(null, user);
            }   
        );
    }
));
