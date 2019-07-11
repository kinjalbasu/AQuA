:- include('clevrKnowledge.pl').
:- include('clevrRules.pl').
:- include('clevrSemanticRules.pl').
:- include('clevrCommonFacts.pl').
find_ans(Q) :- question(Q),find_all_filters(cube, 14, L1),list_object(L1, Ids1),get_att_val(Ids1, color, Val),find_all_filters(thing, 5, L2),list_object([[color,Val]|L2], Ids2),list_subtract(Ids2, Ids1, Ids),list_length(Ids, C),quantification(N, thing_5),gte(C, N).
question('is there any other thing that is the same color as the metal cube ?').
question_answer('is there any other thing that is the same color as the metal cube ?', true) :- find_ans('is there any other thing that is the same color as the metal cube ?').
question_answer('is there any other thing that is the same color as the metal cube ?', false) :- not(find_ans('is there any other thing that is the same color as the metal cube ?')).
query(Q, A) :- question(Q),question_answer(Q, A).
