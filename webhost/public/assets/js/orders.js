$(document).ready(() => {


  firebase.auth().onAuthStateChanged(user => {
    if (user) {
      console.log("User logged in successfully");
      // Set user email address
      $('#user_profile_name').text(user.email || 'Login to get started');

      // Set user profile image
      $('#user_profile_image').attr('src', user.photoURL || './assets/img/placeholders/default-avatar.png');


    } else {
      console.log("No user found");
      window.location = "index.html";
    }
  });

  if (window.localStorage.getItem("newly_added_order")) {
    console.log(window.localStorage.getItem("newly_added_order"));
  }


});

var getStats = () => {
  // Tables
  var orderTable = $('#order_table')

  // Placeholders
  var placeholderOrders = $('#placeholder_orders');

  var ordersquery = firebase.firestore().collection('test_orders').orderBy('timestamp', 'asc').limit(100);
  ordersquery.get().then((snapshot) => {
    if (snapshot.size != 0) {
      placeholderOrders.hide();
    }

    // Snapshot
    return snapshot.forEach(doc => {
      if (doc.exists) {
        var data = doc.data();
        // Add data to table
        orderTable.append(`<tr><th>${data.firstName} ${data.lastName}</th><td>${data.address}</td><td>${data.city}</td><td id="data_status"></td></tr>`);
        var statusData = $('#data_status');
        if (data.status) {
          statusData.val("Delivered");
        } else {
          statusData.val("Pending");
        }
      }
    });
  }).catch((err) => {
    if (err) {
      return console.log(err.message);
    }
  });
};