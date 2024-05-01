
right_hand = {

  \clef treble
  \key c \major
  \time 4/4

  % Omit the base or treble clef
  % \omit Staff.Clef

  % Hide the time signature (e.g. 4/4)
  \omit Staff.TimeSignature

  % Hide the bar lines
  % \omit Score.BarLine

  % the highest and lowest notes in the right hand are e''' and f

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










left_hand = {

  \clef bass
  \key c \major
  \time 4/4

  \omit Staff.TimeSignature

   % the highest and lowest notes in the base are b' and e,,
   b'  e,,

}







\score {
  \new PianoStaff <<
    \new Staff = "RH"  <<
      \right_hand
    >>
    \new Staff = "LH" <<
      \left_hand
    >>
  >>
}














\header {
  tagline = ""  % removed
}
