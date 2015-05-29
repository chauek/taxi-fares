/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    watchID: 0,
    rideID: 0,
    sending: false,
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.receivedEvent('deviceready');
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        $("#start-button").bind('click', this.startRide)
        $("#taxi-fare-connect-form").css("display", "block")

        console.log('Received Event: ' + id);
    },

    startRide: function() {
        $("#stop-button").bind('click', app.stopRide);
        $("#start-button").hide();
        $("#stop-button").show();
        $("#ride-log").empty();
        $("#ride-log").append("<p>Started</p>");

        var onSuccessPos = function(position) {
            app.sendFirstPosition(position)
        };

        function onErrorPos(error) {
            $("#ride-log").append('<p class="error">code:'    + error.code    + ' ' +
                'message:' + error.message + '</p>');
        }

        console.log("waiting for the first position")
        navigator.geolocation.getCurrentPosition(onSuccessPos, onErrorPos);
    },

    stopRide: function() {
        navigator.geolocation.clearWatch(app.watchID);
        $("#ride-log").append('<p>STOPPED</p>');
        $("#start-button").show()
        $("#stop-button").hide()
    },

    watchPosition: function(rideId) {
        function onSuccess(position) {
            app.sendWatchedPosition(position)
        }

        function onError(error) {
            $("#ride-log").append('<p class="error">code:'    + error.code    + ' ' +
            'message:' + error.message + '</p>');
        }

        // Options: throw an error if no update is received every 30 seconds.
        var options = { enableHighAccuracy: true, timeout: 30000 };
        app.watchID = navigator.geolocation.watchPosition(onSuccess, onError, options);
        console.log(app.watchID);
    },

    sendFirstPosition: function(position) {
        var url = $("#host").val()+'/rides/'+position.coords.latitude+'/'+position.coords.longitude
        console.log(url)
        $.ajax({
            dataType: "json",
            url: url,
            //async: false,
            success: function(data) {
                console.log('success');
                console.log(data);
                app.rideID = data.rideId
                app.watchPosition(data.rideId);
            },
            error: function(object, errorMsg, error) {
                console.log('ERROR:')
                console.log(errorMsg)
                console.log(error)
                $("#ride-log").append('<p class="error">error:' + error + '</p>');
                app.stopRide();
            }
        });
    },

    sendWatchedPosition: function(position) {
        if (!app.sending) {
            app.sending = true
            var url = $("#host").val()+'/rides/'+ app.rideID +'/'+position.coords.latitude+'/'+position.coords.longitude
            console.log(url)
            $.ajax({
                dataType: "json",
                url: url,
                timeout: 5000,
                //async: false,
                success: function(data) {
                    $("#cost").text(data.fare)
                    $("#dist").text(data.distance)
                    $("#time").text(data.time/1000)
                    console.log('success');
                    console.log(data);
                    app.sending = false;
                },
                 error: function(object, errorMsg, error) {
                     console.log('ERROR:')
                     console.log(errorMsg)
                     console.log(error)
                     $("#ride-log").append('<p class="error">errorMsg:' + errorMsg + 'error:' + error + '</p>');
                    app.sending = false;
                 }
            });
        }
        else {
            console.log('waiting for server');
        }
    }
};










