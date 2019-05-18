var express = require('express');
var jwt = require('express-jwt');
var router = express.Router();

var authentication = jwt({
    secret: process.env.JWT_PASSWORD,
    userProperty: 'payload'
});

// controller modules
var ctrlWorkouts = require('../controllers/workouts');
var ctrlAuthentication = require('../controllers/authentication');

// Controllers for working with todo lists
router.get('/workouts/:idUser', authentication, ctrlWorkouts.workoutGetIndices);
router.get('/workouts/:idUser/:idWorkout', authentication, ctrlWorkouts.workoutGetById);
router.post('/workouts/:idUser', authentication, ctrlWorkouts.workoutAddNew)

// Controllers for authentication
router.post('/users', ctrlAuthentication.authSignUp);
router.post('/users/login', ctrlAuthentication.authLogIn);

// Expose router as module.
module.exports = router;
