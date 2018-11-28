  // Initialize Firebase
var firebase = require("firebase/app");

  // Initialize Firebase
  var config = {
    apiKey: "AIzaSyDA2EGuvFzejL9SE0XqZ4SmW6rnQhu5ImM",
    authDomain: "delivery-system-bab22.firebaseapp.com",
    databaseURL: "https://delivery-system-bab22.firebaseio.com",
    projectId: "delivery-system-bab22",
    storageBucket: "delivery-system-bab22.appspot.com",
    messagingSenderId: "1061864876503"
  };

  // Setup configuration
  firebase.initializeApp(config);

  const firestore = firebase.firestore();
  const settings = {/* your settings... */ timestampsInSnapshots: true};
  firestore.settings(settings);
