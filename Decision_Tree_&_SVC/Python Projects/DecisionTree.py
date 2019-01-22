from sklearn import tree
from os import listdir
from array import *
import os.path
word_list=[]
train_value=[[]]
train_label=[]
counter=0
test_label=[]
test_value=[[]]
correct=0
wrong=0
print('running...')
my_path = os.path.join(os.path.dirname(__file__), "train")
if os.path.isdir(my_path):
    for filename in listdir(my_path):
        temp_path = os.path.join(my_path, filename)
        with open(temp_path) as currentFile:
                file_contents = currentFile.read()
                currentFile.close()
                currentlist=file_contents.split()
                for word in currentlist:
                    if word not in word_list:
                        word_list.append(word)
    counter1=0         
    for filename in listdir(my_path):
        temp_path = os.path.join(my_path, filename)
        with open(temp_path) as currentFile:
                file_contents = currentFile.read()
                currentFile.close()
                currentlist=file_contents.split()
                counter2=0
                temp=[]
                counts = dict()

                if filename.startswith('spm'):
                    train_label.insert(counter1, 1)
                else:
                    train_label.insert(counter1,0)

                
                for word in currentlist:
                    if word in counts:
                        counts[word] += 1
                    else:
                        counts[word] = 1

                for word in word_list:
                    if word in currentlist:
                        temp.insert(counter2, counts.get(word))
                    else:
                        temp.insert(counter2, 0)

                    counter2+=1
                                    
                train_value.insert(counter1,temp)
                counter1+=1
    del train_value[len(train_value)-1]
    clf = tree.DecisionTreeClassifier()
    clf = clf.fit(train_value, train_label)
else:
    print('Place training folder in Decision Tree folder')
    
counterA=0

my_path = os.path.join(os.path.dirname(__file__), "test")
if os.path.isdir(my_path):
    for filename in listdir(my_path):
            temp_path = os.path.join(my_path, filename)
            with open(temp_path) as currentFile:
                file_contents = currentFile.read()
                currentFile.close()
                currentlist=file_contents.split()
                counterB=0
                temp2=[]
                counts = dict()

                if filename.startswith('spm'):
                    test_label.insert(counterA, 1)
                else:
                    test_label.insert(counterA, 0)
                    
                for word in currentlist:
                    if word in counts:
                        counts[word] += 1
                    else:
                        counts[word] = 1
                        
                for word in word_list:
                    if word in currentlist:
                        temp2.insert(counterB, counts.get(word))
                    else:
                        temp2.insert(counterB, 0)
                    counterB+=1
                test_value.insert(counterA,temp2)
                counterA+=1

    del test_value[len(test_value)-1]
    i=0

    predict = clf.predict(test_value)
    while i< len(test_value):
            if predict[i]==test_label[i]:
                correct+=1
            else:
                wrong+=1
            i+=1                        
    print(correct/(wrong+correct)*100)
else:
    print('Place test folder in Decision Tree folder')
                    
                    
                    




