



zoo = \relative c'' {
  \omit Staff.Clef
  \clef treble
  \key c \major
  \time 4/4

  a4 b c d
}

lower = \relative c {
  \clef bass
  \key c \major
  \time 4/4

  a2 c
}

\score {
  \new PianoStaff \with { instrumentName = "Piano" }
  <<
    \new Staff = "upper" \zoo
    \new Staff = "lower" \lower
  >>
  \layout { }

  % This generates a midi file:
  % \midi { }
}
