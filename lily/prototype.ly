
rh = {

  \clef treble
  \key e \major

  %\time 4/4

  % Omit the base or treble clef
  % \omit Staff.Clef

  % hide the time signature (e.g. 4/4)
  \omit Staff.TimeSignature

  % Hide the bar lines
  % \omit Score.BarLine

  % the highest and lowest notes in the right hand are e''' and f

  f4 g a  b  c' d' e' f' g' a' b' c'' d'' e'' f'' g'' a'' b'' c''' d''' e'''


  % to draw chords, place in single angle brackets:

  <e g b>2 <cis' e' g'>

}










lh = {

  \clef bass
  \key c \major
  %\time 4/4

  \omit Staff.TimeSignature

   % the highest and lowest notes in the base are b' and e,,
   b'4  e,,

   e,, f,, g,, a,, b,, c, d, e, f, g, a, b, c d e f g a b c' d' e' f' g' a' b'
}






% << and >> represent simultaneous music
%
%
% as opposed to
%
% { }


\score {
  \new PianoStaff <<
    \new Staff   <<
      \rh
    >>
    \new Staff  <<
      \lh
    >>
  >>
}














\header {
  tagline = ""  % removed
}
