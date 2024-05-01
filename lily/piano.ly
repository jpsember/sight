
zebra = \relative   {

  \clef treble
  \key e \major
  \time 4/4

  % Omit the base or treble clef
  % \omit Staff.Clef

  % Hide the time signature (e.g. 4/4)
  \omit Staff.TimeSignature

  % Hide the bar lines
  % \omit Score.BarLine


  % Have some things rendered high and low, to try to ensure a consistent
  % location

  <e' g b>2 <cis e g>


  e4   fis gis a b cis dis e fis e dis

  % <c, e g>2 <f bes c>
  % <f c' e g>1
}


\header {
  tagline = ""  % removed
}

\score {

  \new Staff \zebra
  
}
