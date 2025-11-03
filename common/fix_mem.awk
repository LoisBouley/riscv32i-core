BEGIN {
  addr = 0
  first = 1
}

# Ligne adresse : @xxxx
/^@/ {
  newAddr = strtonum("0x" substr($0, 2))
  if (first) {
    print $0       # garde le premier @xxxx (normalement @0000)
    first = 0
  } else {
    while (addr < newAddr) {
      print "00000000"
      addr++
    }
  }
  next
}

# Ligne(s) de données
{
  for (i = 1; i <= NF; i++) {
    print $i
    addr++
  }
}
