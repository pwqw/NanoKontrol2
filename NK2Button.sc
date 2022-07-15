
NK2Button {

	classvar
	<type_none = 0,
	<type_trigger = 1,
	<type_switch = 2;

	var
	<note,
	<led,
	<>type = 1,

	value = false,
	<change_switch = true,

	press_fn, release_fn, change_fn;

	*new {
		|
			note,
			writer = nil,
			type
		|
		^super.new.initButton(note, writer, type);
	}

	initButton { | arg_note, arg_writer, arg_type |
		note = arg_note;
		led = NK2Led(arg_note, arg_writer, false);
		type = arg_type ?? { type };
	}

	val {
		^value
	}

	val_{ | val |
		switch( type,

			type_trigger, {
				this.doChange;
			},

			type_switch, {
				if( change_switch, {
					this.doChange;
				});
				change_switch = change_switch.not;
			},

			type_none, {
				value = value.not;
				this.change(value);
				if(value,
					{ this.press },
					{ this.release }
				);
			}
		);

		^value
	}

	setVal_{ |val|
		value = val;
		if( value, { led.on }, { led.off });
		this.change(value);
		if(value,
			{ this.press },
			{ this.release }
		);
		^value;
	}

	valNotTrig_{ |val|
		value = val;
		if( value, { led.on }, { led.off });
		^value;
	}

	doChange {
		value = value.not;
		if( value, { led.on }, { led.off });
		this.change(value);
		if(value,
			{ this.press },
			{ this.release }
		);
	}

	change {
		^change_fn.value(this, value);
	}

	change_{ | fn |
		^change_fn = fn;
	}

	press {
		^press_fn.value(this, value);
	}

	press_{ | fn |
		^press_fn = fn;
	}

	release {
		^release_fn.value(this, value);
	}

	release_{ | fn |
		^release_fn = fn;
	}
}
