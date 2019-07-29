:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q, A) :- question(Q),find_all_filters(object, 15, L1),list_object(L1, Ids1),get_att_val(Ids1, material, Val),find_all_filters(thing, 5, L2),list_object([[material,Val]|L2], Ids2),list_length(Ids2, A).
question('what number of cyan things are made of the same material as the red object ?').
question_answer('what number of cyan things are made of the same material as the red object ?', A) :- find_ans('what number of cyan things are made of the same material as the red object ?', A).
query(Q, A) :- question(Q),question_answer(Q, A).
