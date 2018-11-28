$(document).ready(() => {

    firebase.auth().onAuthStateChanged(user => {
        if (user) {
            console.log("User logged in successfully");
            // Set user email address
            $('#user_profile_name').text(user.email || 'Login to get started');

            // Set user profile image
            $('#user_profile_image').attr('src', user.photoURL || './assets/img/placeholders/default-avatar.png');

            getRiders();

        } else {
            console.log("No user found");
            window.location = "index.html";
        }
    });

});

// Empty array of riders
var ids = [];

// Get all riders from the database
var getRiders = () => {
    // Tables
    var ridersTable = $('#riders_table');

    var query = firebase.firestore().collection('riders').orderBy('address', 'asc');
    query.get().then((snapshot) => {
        // Snapshot
        return snapshot.forEach(doc => {
            if (doc.exists) {
                var obj = doc.data();
                ids.push(obj);

                // Add data to table
                ridersTable.append(`<tr><th>${obj.name}</th><td>${obj.uid}</td><td>Available</td><td><a href="#" onclick="assignRider('${obj.uid}')" id="rider_assign_btn" class="btn btn-success">Assign</a></td></tr>`);
            }
        });
    }).catch((err) => {
        if (err) {
            return console.log(err.message);
        }
    });
};

// Create Order object
var order = null;

//   Assign Rider
var assignRider = (data) => {
    var assignmentBtn = $('#rider_assign_btn'); //Get button
    assignmentBtn.text("Assigned"); //Change text
    assignmentBtn.addClass("btn btn-default disabled"); //Disable button

    return ids.forEach((value, index, arr) => {
        if (value.uid === data) {
            return order = value;
        }
    });
};

// Submit Order to database
var submitOrder = () => {
    const _val = order;

    // Get all values from the fields
    const fName = $('#input-first-name').val();
    const lName = $('#input-last-name').val();
    const userUID = $('#input-user-uid').val();
    const address = $('#input-address').val();
    const phone = $('#input-phone').val();
    const city = $('#input-city').val();
    const country = $('#input-country').val();
    const postalCode = $('#input-postal-code').val();

    // Check validity of
    if (fName === '' || lName === '' || userUID === '' || address === '' || phone === '' || city === '') {
        alert("Please enter all the necessary details to continue...");
    } else if (order === null) {
        alert("Assign a rider to this order...")
    } else {
        // Update Order json object
        order = {
            rider: _val.uid,
            name: fName + " " + lName,
            uid: userUID,
            address: {
                lat: 5.65,
                lng: -0.18
            },
            phone: phone,
            city: city,
            country: country,
            postalCode: parseInt(postalCode),
            timestamp: new Date().getTime(),
            status: false,
            key: ""
        }



        // Push to database
        const doc = firebase.firestore().collection('orders').doc();
        order.key = doc.id;
        return doc.set(order)
            .then((result) => {
                clearAllFields();
                console.log("Order sent successfully", result);
                return doc.update({
                    key: doc.id
                }).then((_res) => {
                    window.localStorage.setItem("newly_added_order", order);
                    return window.location = 'orders.html';
                }).catch((_err) => {
                    if (_err) {
                        return alert("An error occurred while updating your order. ", _err.message);
                    }
                });
            })
            .catch((err) => {
                return console.log(err.message);
            });
    }
};

// Clears all the fields
var clearAllFields = () => {
    $('#input-first-name').val('');
    $('#input-last-name').val('');
    $('#input-user-uid').val('');
    $('#input-address').val('');
    $('#input-phone').val('');
    $('#input-city').val('');
};