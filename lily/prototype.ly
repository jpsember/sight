
rh = {
  \key e \major
  \clef treble

  %\time 4/4          % Specify time sig
  % \omit Staff.Clef
  \omit Staff.TimeSignature

  % \omit Score.BarLine  % hide the bar lines

  % the highest and lowest notes in the right hand are e''' and f

  f4 g a  b  c' d' e' f' g' a' b' c'' d'' e'' f'' g'' a'' b'' c''' d''' e'''

  % to draw chords, place in single angle brackets:
  <e g b>2 <cis' e' g'>
}






lh = {
  \key e \major

  \clef bass

  \omit Staff.TimeSignature

   % the highest and lowest notes in the base are b' and e,,
   b'4  e,,

   e,, f,, g,, a,, b,, c, d, e, f, g, a, b, c d e f g a b c' d' e' f' g' a' b'

   \relative {
   e,, f g a b c d
   }
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
