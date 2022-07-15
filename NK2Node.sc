
NK2Node : NK2Reader {
	classvar
  	<>node_root;

	var
	<parent,
	<>slots,
	<>proxy_space,

	<type = 'node',
	<type_node = 'node',
	<type_instrument = 'inst',

	<>test_instrument, <>instruments_selector, <selector_path;

	*new {
		|
			server,
			writer,
			selector_path,
			proxy_space = nil,
			parent = nil,
			cant_slots = 8
		|
		^super.new.initNK2Node(server, writer, selector_path, proxy_space, parent, cant_slots);
	}

	initNK2Node {
		|
			arg_server,
			arg_writer,
			arg_selector_path,
			arg_proxy_space,
			arg_parent,
			cant_slots
		|
		server = arg_server;
		parent = arg_parent;
		selector_path = arg_selector_path;
		slots = nil!cant_slots;

		"--------".postln;
		if( parent == nil, {
			// new ProxySpace
			if( proxy_space == nil, {
				proxy_space = ProxySpace(server, name: \ProxySpaceRoot, clock: TempoClock.default);
				proxy_space.quant = 1.0;
			}, {
				proxy_space = arg_proxy_space;
			});
			"NK2Node inicializando nodo root".postln;
			parent = \root;
			super.initNK2Reader(server, arg_writer);
			node_root = this;
		}, {
			"NK2Node inicializando nodo hijo".postln;
			proxy_space = parent.proxy_space;
			super.initNK2Reader(server, parent.writer);
		});
		selector_path ?? { parent !? { selector_path = parent.selector_path } };
		selector_path !? {
			instruments_selector = selector_path.asAbsolutePath.load.(selector_path, this);
		};
		this.initDefaultsEv();
		this.nodeProxy = {
			(this.nodeSlot(0).ar * faders[0].kr) +
			(this.nodeSlot(1).ar * faders[1].kr) +
			(this.nodeSlot(2).ar * faders[2].kr) +
			(this.nodeSlot(3).ar * faders[3].kr) +
			(this.nodeSlot(4).ar * faders[4].kr) +
			(this.nodeSlot(5).ar * faders[5].kr) +
			(this.nodeSlot(6).ar * faders[6].kr)
		};
		test_instrument = NK2Instrument(this);
		^this;
	}

	initDefaultsEv {
		rbuttons.do({ | button, pos |
			button.press = {
				this.activate_or_create(pos, type_node)
			}
		});
		mbuttons.do({ | button, pos |
			button.release = {
				this.activate_or_create(pos, type_instrument)
			}
		});
		backwards_button.release = { |button, val|
			(parent != \root).if({ parent.activate },{ "NK2Node root".postln })
		};
		stop_button.release = { |button, val|
			test_instrument !? { test_instrument.activate }
		};
	}

	activate_or_create {
		|
			pos = 0,
			type = 'node'
		|
		if( slots[pos] == nil, {
			"NK2Node slot[%] not exist. Creando. ".format(pos).postln;
			switch( type,
				type_node, { this.setSlot( NK2Node(server, parent:this), pos )},
				type_instrument, { instruments_selector.(this, pos) }
			);
		}, {
			Task({ // esto fierito es porque sino se activa antes que la señal midi termine
				0.1.wait;
				slots[pos].activate;
			}).start;
		});
		^slots[pos];
	}

	setSlot { |nk2reader, pos|
		slots[pos] = nk2reader;
		Task({ // esto fierito es porque sino se activa antes que la señal midi termine
			0.1.wait;
			nk2reader.activate;
		}).start;
		this.nodeSlot(pos, nk2reader.nodeProxy);
	}

	activate {
		faders.do({ |c| c.matched = false });
		knobs.do({ |c| c.matched = false });
		this.restartLedControlButtons();
		sbuttons.do({ |button| button.led.off });
		slots.do({ |slot, i|
			if( slot == nil, {
				mbuttons[i].led.off;
				rbuttons[i].led.off;
			}, {
				switch( slot.type,
					type_instrument, {
						mbuttons[i].led.on;
						rbuttons[i].led.off;
						"El slot % es %".format(i, slot).postln;
					},
					type_node, {
						mbuttons[i].led.off;
						rbuttons[i].led.on;
						"El slot % es %".format(i, slot).postln;
					}
				)
			});
		});
		super.activate;
	}

	nodeProxy {
		^proxy_space[uid.asSymbol]
	}

	nodeProxy_{ |source|
		proxy_space[uid.asSymbol].source = source;
	}

	nodeSlot { |pos, source|
		var name = (uid.asString ++ "~" ++ pos.asString).asSymbol;
		source !? {
			proxy_space[name].source = source;
		};
		^proxy_space[name]
	}

	play {
		this.nodeProxy.play;
	}

	free {
		slots.do({ |slot|
			slot.free;
		});
		^super.free
	}
}
