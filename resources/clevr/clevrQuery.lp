:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q, A) :- question(Q),find_all_filters(cube, 10, L1),list_object(L1, Ids1),get_att_val(Ids1, color, Val),find_all_filters(thing, 3, L2),list_object([[color,Val]|L2], Ids2),list_length(Ids2, A).
question('how many things have the same color as the cube ?').
question_answer('how many things have the same color as the cube ?', A) :- find_ans('how many things have the same color as the cube ?', A).
query(Q, A) :- question(Q),question_answer(Q, A).
