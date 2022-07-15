
NK2Control {

	var
	<server,
	<>matched_led,
	<init_val,
	<>precision,

	<midival,
	<matched = false,
	change_fn,

	<bus;

	*new {
		|
		server,
		matched_led = nil,
		init_val = 0,
		precision = 3
		|
		^super.newCopyArgs(server, matched_led, init_val, precision).initNK2Control;
	}

	initNK2Control {
		bus = Bus.control(server, 1);
		this.val = init_val;
	}

	matched_ { |state|
		if (matched != state, {
			matched = state;
			matched_led !? {
				if( matched, { matched_led.on }, { matched_led.off });
			};
		});
	}

	midival_ { |v|
		if( (midival - precision < v) && (midival + precision > v), {
			this.setMidival = v;
			this.matched = true;
			this.change;
		}, {
			this.matched = false;
		});
	}

	setMidival_ { |v|
		bus.value = v/127;
		if( (midival - precision < v) && (midival + precision > v), {
			this.matched = true;
		}, {
			this.matched = false;
		});
		midival = v;
		^v;
	}

	val_ { |v|
		bus.value = v;
		midival = (v*127).round;
		^v;
	}

	val {
		^bus.getSynchronous;
	}

	kr { |minval=0, maxval=1, warp=0|
		// like a MouseX.kr
		if(minval==0 && maxval==1, {
			^bus.kr;
		}, {
			if(warp==0, {
				^bus.kr.linlin(0,1.0, minval, maxval)
			}, {
				^bus.kr.linexp(0,1.0, minval, maxval)
			})
		})
	}

	free {
		bus.free;
		matched_led = nil;
		^super.free;
	}

	change {
		^change_fn.value(this, bus);
	}

	change_{ | fn |
		^change_fn = fn;
	}

}

// To be expanded later...

NK2Knob : NK2Control {}

NK2Fader : NK2Control {}
