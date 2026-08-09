#!/system/bin/sh

ui_print "Installing the removable Xiaohei DSP device profile"
set_perm_recursive "$MODPATH/system/system_ext" 0 0 0755 0644

rnn_plugin="$MODPATH/system/vendor/lib/libcapiv2svarnn.so"
if [ -f "$rnn_plugin" ]; then
  # The 32-bit vendor HAL dlopens this plugin from its vendor linker namespace.
  set_perm "$rnn_plugin" 0 0 0644 u:object_r:vendor_file:s0
fi
