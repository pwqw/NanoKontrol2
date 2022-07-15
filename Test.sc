
A {
	var <>mididef,
	value = false,
	other = true;

	init {
		 mididef = { |a| (1).postln; };
	}

	val {
		^value
	}

	val_{ | val |
		if(other, {
			value = val;
		});
		other = other.not;
		^value
	}
}
