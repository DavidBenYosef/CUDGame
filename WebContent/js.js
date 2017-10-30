$.getScript("validate_spanish_id.js");

var loc = window.location.pathname;
var dir = loc.substring(0, loc.lastIndexOf('/'));
var baseUri = "ws://" + window.location.host + dir + "/User";

var timePerRound;

var totalRounds;
var round;

var newSelection;

function init() {

}

function connect() {
	if ($("#name").val() == "" || $("#uid").val() == "") {
		setInfo("{SET_NAME_ID}");
		return;
	}

	if (!isIDValid($("#uid").val())) {
		setInfo("{ID_INVALID}");
		return;
	}

	else {
		$("#login").hide();
		var wsUri = baseUri + "/" + $("#uid").val() + "/" + $("#name").val();

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

function isIDValid(id) {
	// var res = ValidateSpanishID(id);

	// return res.valid;

 return isLegalTz($("#uid").val());

	//return true;
}

function onOpen(evt) {

	resetData();
	$("#user").text($("#name").val());

	setInfo("{CONNECTING}");

	$("#login").hide();
	$("#logout").show();
	$("#wrapper").show();

	window.onbeforeunload = function askConfirm() {
		return "Your connection will be lost!";
	};
}
function onClose(evt) {

	if (evt.reason != "") {

		setInfo(String(evt.reason));
	} else {
		setInfo("{DISCONNECTED}");
	}
	;

	$("#login").show();
	$("#logout").hide();
	$("#wrapper").hide();

	$("#timer").TimeCircles().destroy();
	resetData();
	window.onbeforeunload = null;
}

function onMessage(evt) {

	var message = JSON.parse(evt.data)

	if (message.text != null) {
		setInfo(message.text);
	}

	if (message.round != null) {
		round = message.round;
		$("#round").show();
		buildRoundsCircle(round);
	}

	if (message.totalRounds != null) {
		totalRounds = message.totalRounds;
	}

	if (message.isIntro != null) {
		if (message.isIntro) {
			$("#introGame").show();
		} else {
			$("#introGame").hide();
		}
	}

	if (message.orderedCands != null && message.results != null
			&& message.currentSelection != null) {
		newSelection = null;
		$("#select").html(
				buildCands(message.orderedCands, message.results,
						message.currentSelection));
		if (message.type == "NEW_ROUND") {
			setCountdownTimer();
		}
	}

	if (message.voters != null) {
		$("#voters").text(message.voters);
		$("#users").show();
	}

	if (message.totalVoters != null) {
		$("#totalVoters").text(message.totalVoters);
	}

	if (message.gameId != null) {
		$("#gameId").text(message.gameId);
	}

	if (message.type == "DECIDED" || message.type == "DEADLINE") {
		$(".link").attr("onclick", null);
		$("#newRound").show();
		if (message.type == "DECIDED") {
			setInfo("{AGREEMENT} <span></span>"
					+ calculateScore(message.orderedCands.length,
							message.orderedCands
									.indexOf(message.currentSelection)));
		} else {
			setInfo("{NO_AGREEMENT}");
		}

	}

	if (message.timePerRound != null) {
		timePerRound = message.timePerRound;
	}
	if (message.myscore != null) {
		setInfo(message.myscore);
	}
}

function setCountdownTimer() {
	var timer = document.createElement("div");
	timer.setAttribute("id", "timer");
	timer.setAttribute("data-timer", timePerRound);
	$("#CountDownTimer").html(timer);
	$("#timer").TimeCircles({
		time : {
			Days : {
				show : false
			},
			Hours : {
				show : false
			},
			Minutes : {
				show : false
			},
			Seconds : {
				color : "#dcae3e"
			}
		},
		count_past_zero : false,
		circle_bg_color : "#000000"
	}).addListener(function(unit, value, total) {
		var time = $("#timer").TimeCircles().getTime();
		//$("#test").html(time);
		//$("#test1").html(value);
		if (value == 0) {
			send_message("SELECT", round, newSelection);
			setInfo("{WAIT_FOR_OTHER}");
			$("#timer").TimeCircles().destroy();
			$("#CountDownTimer").empty();
			$("#select").empty();
		}
	});
}

function onError(evt) {
	alert('Server is down');
}

function send_message(msgType, roundNum, selected) {
	var msg = {
		type : msgType,
		round : roundNum,
		selectedCand : selected
	};

	doSend(msg);
}

function closeSocket() {
	websocket.close();
}

function doSend(message) {
	websocket.send(JSON.stringify(message));

}

function setInfo(message) {
	$("#info").fadeOut();
	$("#info").empty();
	$("#info").html(message);
	$("#info").fadeIn();
}

function resetData() {
	$("#users").hide();
	$("#rounds").hide();
	$("#time").empty();
	$("#voters").empty();
	//$("#info").empty();
	$("#select").empty();
	$("#totalVoters").empty();
	$("#currentSelection").empty();
	$("#gameId").empty();
	$("#newRound").hide();
	$("#round").hide();
	$("#introGame").hide();
}

function newRound() {
	resetData();
	send_message("NEWGAME", null, null);

}

function buildCands(orderedCands, map, currentSelection) {
	var candsContainer = document.createElement("div");
	candsContainer.setAttribute("id", "candsContainer");

	for (i = 0; i < orderedCands.length; i++) {

		var cand = orderedCands[i];
		var result = map[cand];

		var candDiv = document.createElement("div");
		candDiv.setAttribute("class", "candDiv");

		var candTotal = document.createElement("div");
		candTotal.setAttribute("class", "candTotal candTotal" + cand);
		candTotal.innerHTML = result;
		candDiv.appendChild(candTotal);

		var candResults = document.createElement("div");
		candResults.setAttribute("class", "candResults");

		for (j = 0; j < result; j++) {
			var candline = document.createElement("div");
			candline.setAttribute("class", "candLine candLine" + cand);
			candResults.appendChild(candline);
		}

		candDiv.appendChild(candResults);

		var link = document.createElement("a");
		link.setAttribute("id", "candLink" + cand);

		if (cand == currentSelection) {
			link.setAttribute("class", "selectedLink");
			link.setAttribute("OnClick", "selectCand(null)");
		} else {
			link.setAttribute("class", "link");
			link.setAttribute("OnClick", "selectCand(" + cand + ")");
		}

		var candImg = document.createElement("img");
		candImg.src = "gfx/" + cand + ".jpg"
		candImg.id = "picture"
		link.appendChild(candImg);

		var score = document.createElement("span");
		score.setAttribute("class", "score");
		score.setAttribute("lang", "code");
		score.innerHTML = "{SCORE}<br/>"
				+ calculateScore(orderedCands.length, i);
		link.appendChild(score);

		candDiv.appendChild(link);

		candsContainer.appendChild(candDiv);
	}
	return candsContainer;
}

function selectCand(cand) {
	newSelection = cand;
	$(".nextSelectedLink").attr("class", "link");
	if (cand != null) {
		$("#candLink" + cand).attr("class", "nextSelectedLink");
		setInfo("{WAIT_FOR_OTHER}");
	} else {
		setInfo("{WANT_TO_CHANGE}");
	}
}

function calculateScore(candsNum, candIndex) {
	return Math.round(100 / candsNum * (candsNum - candIndex));
}

function getMyScore() {
	send_message("MYSCORE", null, null);
}

function showPage() {
	$("#intro").hide();
	$("#page").show();
	document.body.style.backgroundColor = "#536e32";
}

function isLegalTz(num) {
	var tot = 0;
	var tz = new String(num);
	for (i = 0; i < 8; i++) {
		x = (((i % 2) + 1) * tz.charAt(i));
		if (x > 9) {
			x = x.toString();
			x = parseInt(x.charAt(0)) + parseInt(x.charAt(1))
		}
		tot += x;
	}

	if ((tot + parseInt(tz.charAt(8))) % 10 == 0) {
		return true;
	} else {
		return false;
	}
}

function buildRoundsCircle(rounds) {
	Circles.create({
		id : 'roundCircle',
		radius : 40,
		value : rounds,
		maxValue : totalRounds,
		width : 40,
		text : function(value) {
			return value;
		},
		colors : [ '#f26a44', '#d3ce3e' ],
		duration : 0
	});
}

function switchLang(locale) {
	if (locale == "he") {
		document.body.style.direction = "rtl";
	} else {
		document.body.style.direction = "ltr";
	}
	lang.change(locale);
}

function agreement(element) {
	if (element.checked) {
		$("#gotit").prop("disabled", false);
	} else {
		$("#gotit").prop("disabled", true);
	}
}

window.addEventListener("load", init, false);