/**
 * 
 */

var loc = window.location.pathname;
var dir = loc.substring(0, loc.lastIndexOf('/'));
var baseUri = "ws://" + window.location.host + dir + "/Admin";

var gameFlag;

// function init() {
// output = document.getElementById("output");
// log = document.getElementById("log");
// change = document.getElementById("change");
// profile = document.getElementById("profile");
// }

function connect() {

	if ($("#pass").val() == "") {
		alert("please provide password");
		return;
	} else {
		var wsUri = baseUri + "/" + $("#pass").val();

		// alert(wsUri);
		websocket = new WebSocket(wsUri);
		websocket.onopen = function(evt) {
			onOpen(evt)
		};
		websocket.onclose = function(evt) {
			onClose(evt)
		};
		websocket.onmessage = function(evt) {
			onMessage(evt)
		};
		websocket.onerror = function(evt) {
			onError(evt)
		};
	}
}

function onOpen(evt) {
	$("#login").hide();
	$("#props").show();
	$("#server").val(window.location.host + dir);
	window.onbeforeunload = function askConfirm() {
		return "Your connection will be lost!";
	};
}
function onClose(evt) {
	$("#login").show();
	$("#props").hide();
	$("#log").val('');
	window.onbeforeunload = null;
}

function onMessage(evt) {

	var message = JSON.parse(evt.data)
	if (message.text != null) {
		addLog(message.text);
	}
	if (message.gameActiveFlag != null) {
		gameFlag = message.gameActiveFlag;
		setGameVisibility(gameFlag);
	}
	if (message.voters != null) {
		$("#voters").val(message.voters);
	}
	if (message.rounds != null) {
		$("#rounds").val(message.rounds);
	}
	if (message.timePerRound != null) {
		$("#timePerRound").val(message.timePerRound);
	}

	if (message.candsSet != null) {
		$('input:radio[name=cands][value=' + message.candsSet + ']').click();
		//$("#candsSet").val(message.candsSet);
	}

	if (message.gamesLimit != null) {
		$("#gamesLimit").val(message.gamesLimit);
	}
	if (message.scoreLimit != null) {
		$("#scoreLimit").val(message.scoreLimit);
	}
	if (message.introGames != null) {
		$("#introGames").val(message.introGames);
	}
	if (message.agentTimer != null) {
		$("#agentTimer").val(message.agentTimer);
	}

	// var $timeout = angular.element(appElement).timeout();
	// $timeout(function () { log.innerHTML = ""; }, 3000);

	// alert(message.type);
	// if (message.type == "FINISH") {
	// $("#Start").prop('disabled', false);
	// $("#Stop").prop('disabled', true);
	// }
	// if (message.type == "PROFILE") {
	// profile.hidden = false;
	// writeToScreen(profile, message.text);
	// }

}

// function buildOptions(orderedCands)
// {
// var form = document.createElement("form");
// for (i = 0; i < cars.orderedCands; i++) {
// var button = document.createElement("button");
// button.value = orderedCands[i];
// button.innerHTML = orderedCands[i];
// form.appendChild(button);
// }
// return form;
// }

function onError(evt) {
	// $log, 'ERROR: ' + evt.data);
}

//function addGame() {
//	
//	// $("#Start").prop('disabled', true);
//	// $("#Stop").prop('disabled', false);
//
//}

function addAgent() {
	var agentsNum = $("#agentsNum").val();
	if (agentsNum == "" || isNaN(agentsNum)) {
		alert("Invalid agents num");
		return;
	}
	var msg = {
		type : "ADD_AGENT",
		text : $("#server").val(),
		agentsNum : agentsNum
	}
	doSend(msg);
}

function sendTxt(txt) {
	var msg = {
		type : txt
	}
	doSend(msg);
}

function closeSocket() {
	websocket.close();
}

function doSend(message) {
	websocket.send(JSON.stringify(message));

}

function addLog(message) {
	$("#log").val($("#log").val() + '\n' + message);
	$("#log").scrollTop($("#log")[0].scrollHeight);
}

function toggleGame() {
	if (switchGame(!gameFlag)) {
		setGameVisibility(gameFlag);
	}
}

function setGameVisibility(flag) {
	if (flag) {
		$("#checkLabel").text("Game Active");

	} else {
		$("#checkLabel").text("Game Inactive");
	}
	$("#addAgent").attr("disabled", !flag);
	$("#voters").attr("readonly", flag);
	$("#rounds").attr("readonly", flag);
	$("#timePerRound").attr("readonly", flag);
	$("#remove").attr("readonly", flag);
	$("#gamesLimit").attr("readonly", flag);
	$("#scoreLimit").attr("readonly", flag);
	$("#agentTimer").attr("readonly", flag);
	$("#introGames").attr("readonly", flag);
	$('input:radio[name=cands]').attr("readonly", flag);
}

function switchGame(flag) {
	if (flag) {
		var rounds = $("#rounds").val();
		var voters = $("#voters").val();
		var timePerRound = $("#timePerRound").val();
		var prefset = $("input[name='cands']:checked").val();
		if (rounds == "" || isNaN(rounds) || voters == "" || isNaN(voters)
				|| timePerRound == "" || isNaN(timePerRound) || prefset == null) {
			alert("Missing or Invalid games setting");
			return false;
		}

		var gamesLimitInput = $("#gamesLimit").val();
		var scoreLimitInput = $("#scoreLimit").val();
		var introGamesInput = $("#introGames").val();

		if ((introGamesInput != "" && isNaN(introGamesInput))
				|| (gamesLimitInput != "" && isNaN(gamesLimitInput))
				|| (scoreLimit != "" && isNaN(scoreLimitInput))) {
			alert("Invalid limit input");
			return false;
		}
		var agentTimerInput = $("#agentTimer").val();
		if (agentTimerInput != "" && isNaN(agentTimerInput)) {
			alert("Invalid agent timer input");
			return false;
		}

		if (gamesLimitInput == "") {
			gamesLimitInput = null;
		}

		if (scoreLimitInput == "") {
			scoreLimitInput = null;
		}
		if (introGamesInput == "") {
			introGamesInput = null;
		}
		if (agentTimerInput == "") {
			agentTimerInput = null;
		}

		var msg = {
			type : "SWITCH",
			text : window.location.host + dir,
			voters : voters,
			rounds : rounds,
			timePerRound : timePerRound,
			candsSet : prefset,
			flag : true,
			gamesLimit : gamesLimitInput,
			scoreLimit : scoreLimitInput,
			introGames : introGamesInput,
			agentTimer : agentTimerInput
		};

		doSend(msg);

	} else {
		var msg = {
			type : "SWITCH",
			flag : false
		};

		doSend(msg);

	}
	gameFlag = flag;
	return true;
}

// window.addEventListener("load", init, false);
