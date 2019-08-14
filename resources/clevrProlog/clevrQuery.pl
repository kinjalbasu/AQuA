:-style_check(-singleton).
:-style_check(-discontiguous).
:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
question('does the red cylinder have the same material as the cylinder behind the yellow thing ?').
question_answer('does the red cylinder have the same material as the cylinder behind the yellow thing ?', yes) :- find_ans('does the red cylinder have the same material as the cylinder behind the yellow thing ?').
question_answer('does the red cylinder have the same material as the cylinder behind the yellow thing ?', no) :- not(find_ans('does the red cylinder have the same material as the cylinder behind the yellow thing ?')).
query(Q, A) :- question(Q),question_answer(Q, A).
