#!/usr/bin/env sh

# Download English-language models.
wget -P models/ -nd http://opennlp.sourceforge.net/models-1.5/en-ner-location.bin
wget -P models/ -nd http://opennlp.sourceforge.net/models-1.5/en-ner-organization.bin
wget -P models/ -nd http://opennlp.sourceforge.net/models-1.5/en-ner-person.bin
wget -P models/ -nd http://opennlp.sourceforge.net/models-1.5/en-sent.bin
wget -P models/ -nd http://opennlp.sourceforge.net/models-1.5/en-token.bin

# Download Spanish-language models.
wget -P models/ -nd http://opennlp.sourceforge.net/models-1.5/es-ner-location.bin
wget -P models/ -nd http://opennlp.sourceforge.net/models-1.5/es-ner-organization.bin
wget -P models/ -nd http://opennlp.sourceforge.net/models-1.5/es-ner-person.bin
