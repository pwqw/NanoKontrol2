NK2Reader {
	classvar
	activated_id;

	// variables and default values
	var
	<server, <src_id, <writer = nil, <uid,

	<>verbose,

	<knobs, <faders,

	<sbuttons, <mbuttons, <rbuttons,
	<ctrl_buttons,
	<track_left_button, track_right_button,
	<cycle_button, <set_button, <marker_right_button, <marker_left_button,
	<backwards_button, <forwards_button,
	<stop_button, <play_button, <rec_button,

	<control,

	mididef_fn, mididef_key,
	<ev, <>after_activate;

	var
	<cant_controls = 8,
	<rbutton_note = 64,
	<marker_right_note = 62,
	<marker_left_note = 61,
	<set_note = 60,
	<track_right_note = 59,
	<track_left_note = 58,
	<mbutton_note = 48,
	<cycle_note = 46,
	<rec_note = 45,
	<forwards_note = 44,
	<backwards_note = 43,
	<stop_note = 42,
	<play_note = 41,
	<sbutton_note = 32,
	<knobs_note  = 16,
	<faders_note = 0;

	// No tiene *new porque es abstracta

	initNK2Reader {
		|
		 	arg_server,
			arg_writer = nil,
			arg_mididef = nil,
			arg_verbose = false
		|
		server = arg_server;
		writer = arg_writer;
		verbose = arg_verbose;
		src_id = MIDIIn.findPort("nanoKONTROL2","nanoKONTROL2 MIDI 1").uid;
		uid = UniqueID.next;
		ev = ();

		this.initSMRbuttons;
	    this.initControlButtons;
	    this.initKnobs;
	    this.initFaders;
		// this.initControlGroup;

		mididef_fn = arg_mididef ?? { this.mididefDefault() };
		mididef_key = "nK2_" ++ uid.asString ++ "_default";
		MIDIdef.cc(mididef_key,  { | val, cc, chan, src |
			if( this.isActive, { this.mididef(val, cc, chan, src) });
		}, srcID: src_id);
	}

	initSMRbuttons {
		sbuttons = Array.fill(cant_controls, { |j| NK2Button(sbutton_note + j, writer, NK2Button.type_none) });
		mbuttons = Array.fill(cant_controls, { |j| NK2Button(mbutton_note + j, writer, NK2Button.type_none) });
		rbuttons = Array.fill(cant_controls, { |j| NK2Button(rbutton_note + j, writer, NK2Button.type_none) });
	}

  	initControlButtons {
		ctrl_buttons = [
			cycle_button = NK2Button(cycle_note, writer, NK2Button.type_switch),
			track_left_button = NK2Button(track_left_note, writer),
			track_right_button = NK2Button(track_right_note, writer),
			set_button = NK2Button(set_note, writer),
			marker_left_button = NK2Button(marker_left_note, writer),
			marker_right_button = NK2Button(marker_right_note, writer),
			backwards_button = NK2Button(backwards_note, writer),
			forwards_button  = NK2Button(forwards_note, writer),
			stop_button =  NK2Button(stop_note, writer),
			play_button =  NK2Button(play_note, writer),
			rec_button  =  NK2Button(rec_note, writer),
		];
	}

  	initKnobs {
    	knobs = Array.fill(cant_controls, { NK2Knob(server) });
  	}

  	initFaders {
    	faders = Array.fill(cant_controls, { |i| NK2Fader(server, matched_led: sbuttons[i].led) });
  	}

	initControlGroup { | add = 0|
		control = ();
		control.self = this;
		control.cycle = cycle_button;
		control.track_left = track_left_button;
		control.track_right = track_right_button;
		control.set_button = set_button;
		control.marker_left = marker_left_button;
		control.marker_right = marker_right_button;
		control.backwards = backwards_button;
		control.forwards = forwards_button;
		control.stop_button = stop_button;
		control.play_button = play_button;
		control.rec = rec_button;
		control.sbuttons = sbuttons;
		control.mbuttons = mbuttons;
		control.rbuttons = rbuttons;
		control.faders = faders;
		control.knobs = knobs;
		sbuttons.do({ |btn, i| control.put( ("s"++(i+add)).asSymbol, btn ) });
		mbuttons.do({ |btn, i| control.put( ("m"++(i+add)).asSymbol, btn ) });
		rbuttons.do({ |btn, i| control.put( ("r"++(i+add)).asSymbol, btn ) });
		faders.do({ |ctr, i| control.put( ("f"++(i+add)).asSymbol, ctr ) });
		knobs.do({ |ctr, i| control.put( ("k"++(i+add)).asSymbol, ctr ) });
        ^control;
	}

	/*
	 * DEPRECATED
	 * Hecho para no tener que cambiar todos los i.c que hay por ahi
	*/
	c {
		control ?? { this.initControlGroup };
		^control;
	}
	c1 {
		control ?? { this.initControlGroup(1) };
		^control;
	}

	mididef { | val, cc, chan, src |
		^mididef_fn.value(val, cc, chan, src);
	}

	mididef_{ | fn |
		^mididef_fn = fn;
	}

	mididefDefault {
		^{ | val, cc, chan, src |
			this.evalAction(val, cc);
 		}
	}

	// --------------------------------------------------------------------------
	// define default evalAction
	evalAction { |val, cc| case
		// R
		{ cc >= rbutton_note } {
			if(cc <= (rbutton_note + cant_controls - 2), { // -2 porque el último es general
				var pos = cc - rbutton_note;
				^rbuttons[pos].val = (val != 0);
			})
		}
		// marker
	    { cc == marker_right_note } {
			^marker_right_button.val = (val != 0);
		}
	    { cc == marker_left_note } {
			^marker_left_button.val = (val != 0);
		}
		// set
	    { cc == set_note } {
			^set_button.val = (val != 0);
		}
		// track
	    { cc == track_right_note } {
			^track_right_button.val = (val != 0);
		}
	    { cc == track_left_note } {
			^track_left_button.val = (val != 0);
		}
		// M
		{ cc >= mbutton_note } {
			if(cc <= (mbutton_note + cant_controls - 2), { // -2 porque el último es general
				var pos = cc - mbutton_note;
				^mbuttons[pos].val = (val != 0);
			})
		}
		// 🔁
	    { cc == cycle_note } {
			^cycle_button.val = (val != 0);
		}
	  	// ⏺
	    { cc == rec_note } {
			^rec_button.val = (val != 0);
		}
	  	// ⏩
	    { cc == forwards_note } {
			^forwards_button.val = (val != 0);
		}
	  	// ⏪
	    { cc == backwards_note } {
			^backwards_button.val = (val != 0);
		}
		// ⏹
		{ cc == stop_note} {
			^stop_button.val = (val != 0);
		}
		// ▶️
		{ cc == play_note} {
			^play_button.val = (val != 0);
		}
		// S
		{ cc >= sbutton_note } {
			if(cc <= (sbutton_note + cant_controls - 2), { // -2 porque el último es general
				var pos = cc - sbutton_note;
				^sbuttons[pos].val = (val != 0);
			})
		}
		// knobs
		{ cc >= knobs_note } {
			var pos = cc - knobs_note;
			^knobs[pos].midival = val;
		}
		// faders
		{ cc >= faders_note } {
			var pos = cc - faders_note;
			^faders[pos].midival = val;
		}
	}// end of evalAction
	// --------------------------------------------------------------------------

	activate {
		activated_id = uid;
		^after_activate !? { ^after_activate.value; };
	}

	isActive {
		^activated_id == uid;
	}

  	restartLedControlButtons {
		ctrl_buttons.do({ |button|
			if( button.val, { button.led.on }, { button.led.off })
		});
		/*
		if( track_left_button.val, { track_left_button.led.on }, { track_left_button.led.off });
		if( track_right_button.val, { track_right_button.led.on }, { track_right_button.led.off });
		if( set_button.val, { set_button.led.on }, { set_button.led.off });
		if( marker_left_button.val, { marker_left_button.led.on }, { marker_left_button.led.off });
		if( marker_right_button.val, { marker_right_button.led.on }, { marker_right_button.led.off });
		if( backwards_button.val, { backwards_button.led.on }, { backwards_button.led.off });
		if( forwards_button.val, { forwards_button.led.on }, { forwards_button.led.off });
		if( stop_button.val, { stop_button.led.on }, { stop_button.led.off });
		if( play_button.val, { play_button.led.on }, { play_button.led.off });
		if( rec_button.val, { rec_button.led.on }, { rec_button.led.off });
		*/
	}

	dumpAll {
		("nK2, src_id: " ++ uid.asString).postln;
		cant_controls.do{  |i|
			("knob" ++ i.asString ++ ": " ++ knobs[i].val ++ ", fader" ++ i.asString ++ ": " ++ faders[i].val).postln;
		}
	}

	free {
		MIDIdef(mididef_key).free;
		cant_controls.do{ |i|
			knobs[i].free;
			faders[i].free;
		};
		^super.free;
	}
}
