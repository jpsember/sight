\version "2.24.3"

% Render the following components in parallel
%

<<

\new Staff {

    \clef treble
    \omit Score.BarLine
    \omit Staff.TimeSignature

    % Add extra space between the key signature and the first note
    \override Staff.KeySignature.space-alist.first-note = #'(extra-space . 8)
    % In case there is no key signature, add space from clef
    \override Staff.Clef.space-alist.first-note = #'(extra-space . 8)

    \key e \major
     <b d' a' >2.  <g cis' fis' >4.  <fis a >2  <cis' >2
}


\new Staff {

    \clef bass
    \omit Score.BarLine
    \omit Staff.TimeSignature

    % Add extra space between the key signature and the first note
    \override Staff.KeySignature.space-alist.first-note = #'(extra-space . 8)
    % In case there is no key signature, add space from clef
    \override Staff.Clef.space-alist.first-note = #'(extra-space . 8)

    \key e \major
     <cis f >2.  <a, fis >4  <d, a, >2  <cis >2
}


>>


% Increase the separation between adjacent notes (chords)
%
\layout {
  \context {
    \Score
    \override SpacingSpanner.base-shortest-duration = #(ly:make-moment 1/64)
  }
}


% Suppress the footer
%
\header {
  tagline = ""  % removed
}
