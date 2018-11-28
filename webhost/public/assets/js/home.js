$(document).ready(() => {
    var username = $('#user_profile_name');
    var profile = $('#user_profile_image');

    //Hide view until data is loaded
    username.hide();
    profile.hide();

    firebase.auth().onAuthStateChanged(user => {
        if (user) {
            console.log("User logged in successfully");

            // Show details when data is loaded
            username.show();
            profile.show();

            // Set user email address
            username.text(user.email || 'Login to get started');

            // Set user profile image
            profile.attr('src', user.photoURL || './assets/img/placeholders/default-avatar.png');


        } else {
            console.log("No user found");
            window.location = "index.html";
        }
    });

    // Get stats for business
    getStats();

});

var getStats = () => {
    // Performance
    var perfCount = $('#performance_count');
    var perfPercent = $('#performance_percent');

    // Profit
    var profitCount = $('#profit_count');
    var profitPercent = $('#profit_percent');

    // Overall sales
    var overallSalesCount = $('#overall_sales_count');
    var overallSalesPercent = $('#overall_sales_percent');

    // New users
    var newUsersCount = $('#new_users_count');
    var newUsersPercent = $('#new_users_percent');

    // Tables
    var userTable = $('#user_table'),
        orderTable = $('#order_table'),
        salesTable = $('#sales_table');

    // Placeholders
    var placeholderSales = $('#placeholder_sales'),
        placeholderUsers = $('#placeholder_users'),
        placeholderOrders = $('#placeholder_orders');

    // Loads user information from the user database table
    var salesquery = firebase.firestore().collection('profits').orderBy('order', 'asc').limit(50);
    salesquery.get().then(snapshot => {

        if (snapshot.size != 0) {
            placeholderSales.hide();
        }

        var totalSales = 0.00;

        // Snapshot
        return snapshot.forEach(doc => {
            if (doc.exists) {
                var data = doc.data();
                totalSales = totalSales + data.sales;

                // Get size of users table
                overallSalesCount.text(totalSales);

                // Compute profit
                var percentageMade = (data.profits / data.sales).toFixed(2);

                // Add data to table
                salesTable.append(`<tr><th scope="row">${data.month || "Invalid"}</th><td>${data.sales}</td><td>${data.profits}</td><td>${percentageMade} %</td></tr>`);
            }
        });
    }).catch((err) => {
        if (err) {
            return console.log(err.message);
        }
    });

    // Loads user information from the user database table
    var userquery = firebase.firestore().collection('customers').orderBy('name', 'asc').limit(10);
    userquery.get().then((snapshot) => {
        // Get size of users table
        newUsersCount.text(snapshot.size);

        if (snapshot.size != 0) {
            placeholderUsers.hide();
        }

        // Snapshot
        return snapshot.forEach(doc => {
            if (doc.exists) {
                var data = doc.data();
                // Add data to table
                userTable.append(`<tr><th scope="row"><div class="media align-items-center"><a href="#" onclick="viewUser(${data.profile})" class="avatar rounded-circle mr-3"><img src="${data.profile || "./assets/img/placeholders/default-avatar.png"}"></a><div class="media-body"><span class="mb-0 text-sm">${data.name || "No username found"}</span></div></div></th><td>${data.email}</td><td>${data.uid}</td></tr>`);
            }
        });
    }).catch((err) => {
        if (err) {
            return console.log(err.message);
        }
    });

    const ids = [];

    // Loads orders information from the orders database table
    var ordersquery = firebase.firestore().collection('orders_authentication').orderBy('timestamp', 'asc').limit(10);
    ordersquery.get().then((snapshot) => {

        if (snapshot.size != 0) {
            placeholderOrders.hide();
        }

        // Snapshot
        return snapshot.forEach(doc => {
            if (doc.exists) {
                var data = doc.data();

                ids.push(data);

                // Get date details
                var date = new Date(data.timestamp);

                // Create formatted date
                var dateValue = `${date.getHours()}:${date.getMinutes()} (${date.getMonth()}/${date.getDay()}/${date.getFullYear()})`;

                // Add data to table
                var status = data.status;
                orderTable.append(`<tr><th scope="row"><div class="media align-items-center"><a id="${data.customerId}" href="#" onclick="viewOrder(${doc.data().key})" class="avatar rounded-circle mr-3"><img src="${data.profile || "./assets/img/placeholders/default-avatar.png"}"></a><div class="media-body"><span class="mb-0 text-sm">${data.customer || "No username found"}</span></div></div></th><td>${data.key}</td><td>${dateValue}</td><td id="data_status"></td></tr>`);
                var statusData = $('#data_status');
                if (status) {
                    statusData.text("Delivered");
                } else {
                    statusData.text("Pending");
                }
            }
        });
    }).catch((err) => {
        if (err) {
            return console.log(err.message);
        }
    });
};

var viewUser = (data) => {
    console.log(data);
    // window.location = 'delivery.html';
};

var viewOrder = (data) => {
    console.log(ids);
};

// Get current user
var getUser = (uid, username, profile) => {
    firebase.firestore().collection("users").doc(firebase.auth().currentUser.uid || uid).then(doc => {
        console.log(doc.data());

        if (doc.exists) {
            user.updateProfile({
                displayName: username,
                photoURL: profile,
            }).then(() => {
                // Update successful.
            }).catch((error) => {
                // An error happened.
                if (err) {
                    console.log(err.message);
                }
            });
        }

    }).catch((err) => {
        if (err) {
            console.log(err.message);

        }
    });
};

var logout = () => {
    firebase.auth().signOut().then(() => {}).catch((err) => console.log(err.message));
};