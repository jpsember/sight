
left_hand = {

  \clef treble
  \key c \major
  \time 4/4

  % Omit the base or treble clef
  % \omit Staff.Clef

  % Hide the time signature (e.g. 4/4)
  \omit Staff.TimeSignature

  % Hide the bar lines
  % \omit Score.BarLine

  % the highest note in the treble clef is e'''
  % the lowest is f
  f g a  b  c' d' e' f' g' a' b' c'' d'' e'' f'' g'' a'' b'' c''' d''' e'''



  % Below middle c
  <e g b>2 <cis' e' g'>

  <e' g' b'>2 <cis'' e'' g''>


  e4   fis gis a b cis dis e fis e dis

  % <c, e g>2 <f bes c>
  % <f c' e g>1
}


\score {

  \new Staff \left_hand

}


\header {
  tagline = ""  % removed
}
