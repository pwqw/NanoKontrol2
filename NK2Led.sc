
NK2Led {

	var <note, <writer, <is_on = false;

	*new { arg note, writer = nil, is_on = false;
		^super.new.initLED(note, writer, is_on);
	}

	initLED {arg arg_note, arg_writer, arg_ison;
		note = arg_note;
		writer = arg_writer;
		is_on = arg_ison;

		if ( (is_on != false), {
			is_on.postln;
			this.on;
		}, {
			this.off;
		});
	}


	on {
		if ( (writer != nil), {
			writer.on(note);
			is_on = true;
		});
	}


	off {
		if ( (writer != nil), {
			writer.off(note);
			is_on = false;
		});
	}


	change {
		if ( (is_on != false), {
			this.off;
		}, {
			this.on;
		});

	}


	blink {

		Task({
			3.do{ this.change;  0.1.wait; };
			this.change;
		}).play;

	}

}
