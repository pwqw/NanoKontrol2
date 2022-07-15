c = i.c

c.sbuttons ++ c.mbuttons

"----------------------------------------------".postln;
// Actualiza selector

r = "~/supercollider/instrumentos/01_selector.scd";
o.instruments_selector = nil;
o.instruments_selector = r.load.(PathName(r), o);
o.instruments_selector.(o, 0);

"----------------------------------------------".postln;
// prueba de eventos

e = ();
e.sd();
e.sf('s') ?? { 'a' };
e.sf = { |self, a| a ?? {'b'} };
e['sf'].();

i.play_button.type = NK2Button.type_trigger;
i.ev.play_button = { |button|
	button.val.postln;
};

//Esto era el mididef que hice al principio

o.mididef = { | val, cc, chan, src |
	var action = o.evalAction(val, cc);
	switch( action.type,
		false, { /* pass */ },
		"R_release", { "r R_release".postln },
		"R_press", { "r R_press".postln },
	);
}
o.mididef = o.mididefDefault()


"----------------------------------------------".postln;
// Cosa del principio ahora se usa 0002_init

s.boot;
Server.killAll;

w = NK2Writer(s);
o = NK2Node(s,w).activate;

"----------------------------------------------".postln;
// NodeProxys a full

o.slots[0].nodeProxy.source = { Saw.ar([300, 301.3], 1) };
o.play;
o.proxy_space.postln;

"----------------------------------------------".postln;
// NodeProxys

a = o.nodeSlot(1);
a.source = { Saw.ar([400, 401.3], 1) };
b = o.nodeSlot(1);
b.source = {
	(a * o.faders[0].kr)
};
b.play;
c = o.nodeSlot(2);
c.source = { Saw.ar([200, 201.3], 1) };
b.source = {
	(a * o.faders[0].kr) +
	(c * o.faders[1].kr)
};

"----------------------------------------------".postln;
// ProxySpaces
p = ProxySpace.new(s);
p[\x][0] = { Saw.ar([400, 401.3], 1) };
p[\x][1] = { Saw.ar([100, 101.3], 1) };
p[\z] = (p[\x] * pi).sin * 0.1 * { LFSaw.kr(LFNoise1.kr(0.1 ! 3).sum * -18).max(0.2) };
p[\z].play;
p[\x][1] = nil;

p[\a] = { Saw.ar(o.faders[2].kr*1000) };
p[\b] = { Saw.ar(o.faders[1].kr*1000) };
p[\c] = { p[\a] + p[\b] }
p[\c].play;
p[\c].vol = 1;
p[\a].map(\freq , o.faders[2].kr)
p[\c].free;
p[\c] = nil

p.push

~a.play;
~i1 = { Saw.ar([400, 401.3], 1) };
~i2 = { Saw.ar([100, 101.3], 1) };
~a[0] = ~i1;
~a[1] = ~i2;
~i1.vol = 0.1;
~i2.vol = 0.1;
~a.vol = 1;
~a.stop;

"----------------------------------------------".postln;
// cosas del principio
o.src_id;
o.rbuttons.do({ |button| button.led.blink });
o.slots[1].nodeProxy.vol
o.faders[2].kr

o.activate

"----------------------------------------------".postln;
// Writer
w.on(32);
w.off(32);
