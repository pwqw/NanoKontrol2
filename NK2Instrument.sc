
NK2Instrument : NK2Reader {

	var
	<parent,
	<>slots,
	<>proxy_space,

	<type = 'inst',

	<>activate_text;

	*new {
		|
			parent = nil,
			server = nil,
			proxy_space = nil,
			writer = nil,
			mididef = nil
		|
		^super.new.initNK2Instrument(parent, server, proxy_space, writer, mididef);
	}

	initNK2Instrument {
		|
			arg_parent,
			arg_server,
			arg_proxy_space,
			arg_writer,
			arg_mididef
		|
		var writer;
		parent = arg_parent;
		if (parent != nil, {
			server = parent.server;
			proxy_space = parent.proxy_space;
			writer = parent.writer;
		}, {
			arg_server ?? {
                Server.local ?? {
                ^"NK2Instrument: Si no tiene «parent» por lo menos debe tener «server»".warn;
                };
                arg_server = Server.local;
                "NK2Instrument: No tiene «parent» usando default «server: s»".postln;
			};
            server = arg_server;
			proxy_space = arg_proxy_space ?? ProxySpace(server, name: \ProxySpaceInstrument, clock: TempoClock.default);
			writer = arg_writer ?? NK2Writer(server);
		});
		super.initNK2Reader(server, writer, arg_mididef);
		this.initDefaultsEv();
		this.nodeProxy.ar;
	}

  	initKnobs {
    	knobs = Array.fill(cant_controls, { |i| NK2Knob(server, matched_led: mbuttons[i].led) });
  	}

	initDefaultsEv {
		backwards_button.press = {
			parent.activate
		};
	}

	activate {
		sbuttons.do({ |button| if(button.val, { button.led.on }, { button.led.off }); });
		mbuttons.do({ |button| if(button.val, { button.led.on }, { button.led.off }); });
		rbuttons.do({ |button| if(button.val, { button.led.on }, { button.led.off }); });
		faders.do({ |c| c.matched = false });
		knobs.do({ |c| c.matched = false });
		this.restartLedControlButtons();
		activate_text ?? { activate_text = "Instrumento activado" };
		activate_text.postln;
		^super.activate;
	}

	nodeProxy {
		^proxy_space[uid.asSymbol]
	}

	nodeProxy_{ |source|
		proxy_space[uid.asSymbol].source = source;
	}
}
