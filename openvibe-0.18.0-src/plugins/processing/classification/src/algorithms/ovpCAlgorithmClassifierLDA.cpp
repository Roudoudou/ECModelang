#include "ovpCAlgorithmClassifierLDA.h"

#if defined TARGET_HAS_ThirdPartyITPP

#include <map>
#include <sstream>
#include <cmath>

#include <xml/IXMLNode.h>
#include <xml/IXMLHandler.h>

static const char* const c_sTypeNodeName = "LDA";
static const char* const c_sClassesNodeName = "Classes";
static const char* const c_sCoeffNodeName = "Coefficients";

extern const char* const c_sClassifierRoot;



OpenViBE::int32 OpenViBEPlugins::Classification::getLDABestClassification(OpenViBE::IMatrix& rFirstClassificationValue, OpenViBE::IMatrix& rSecondClassificationValue)
{
	if(ov_float_equal(rFirstClassificationValue[0], ::fabs(rSecondClassificationValue[0])))
		return 0;
	else if(::fabs(rFirstClassificationValue[0]) < ::fabs(rSecondClassificationValue[0]))
		return -1;
	else
		return 1;
}

using namespace OpenViBE;
using namespace OpenViBE::Kernel;
using namespace OpenViBE::Plugins;

using namespace OpenViBEPlugins;
using namespace OpenViBEPlugins::Classification;

using namespace OpenViBEToolkit;


boolean CAlgorithmClassifierLDA::initialize(void)
{
	TParameterHandler < XML::IXMLNode* > op_pConfiguration(this->getOutputParameter(OVTK_Algorithm_Classifier_OutputParameterId_Configuration));
	op_pConfiguration=NULL;

	return CAlgorithmClassifier::initialize();
}

boolean CAlgorithmClassifierLDA::train(const IFeatureVectorSet& rFeatureVectorSet)
{
	std::map < float64, uint64 > l_vClassLabels;
	for(size_t i=0; i<rFeatureVectorSet.getFeatureVectorCount(); i++)
	{
		l_vClassLabels[rFeatureVectorSet[i].getLabel()]++;
	}

	if(l_vClassLabels.size() != 2)
	{
		this->getLogManager() << LogLevel_Error << "A LDA classifier can only be trained with 2 classes, not more, not less - got " << static_cast<uint32>(l_vClassLabels.size()) << "\n";
		return false;
	}

	uint32 l_ui32NumberOfFeatures=rFeatureVectorSet[0].getSize();
	uint32 l_ui32NumberOfFeatureVectors=rFeatureVectorSet.getFeatureVectorCount();

	m_f64Class1=l_vClassLabels.begin()->first;
	m_f64Class2=l_vClassLabels.rbegin()->first;

	itpp::vec l_oMeanFeatureVector1(l_ui32NumberOfFeatures);
	itpp::vec l_oMeanFeatureVector2(l_ui32NumberOfFeatures);

	l_oMeanFeatureVector1.zeros();
	l_oMeanFeatureVector2.zeros();

	for(size_t i=0; i<rFeatureVectorSet.getFeatureVectorCount(); i++)
	{
		const IFeatureVector& l_rFeatureVector=rFeatureVectorSet[i];
		const float64 l_f64Label=l_rFeatureVector.getLabel();

		if(l_f64Label==m_f64Class1)
		{
			l_oMeanFeatureVector1+=itpp::vec(l_rFeatureVector.getBuffer(), l_rFeatureVector.getSize());
		}

		if(l_f64Label==m_f64Class2)
		{
			l_oMeanFeatureVector2+=itpp::vec(l_rFeatureVector.getBuffer(), l_rFeatureVector.getSize());
		}
	}

	l_oMeanFeatureVector1/=static_cast<float64>(l_vClassLabels[m_f64Class1]);
	l_oMeanFeatureVector2/=static_cast<float64>(l_vClassLabels[m_f64Class2]);

	itpp::vec l_oDiff;
	itpp::mat l_oSigma(l_ui32NumberOfFeatures, l_ui32NumberOfFeatures);
	l_oSigma.zeros();

	for(size_t i=0; i<rFeatureVectorSet.getFeatureVectorCount(); i++)
	{
		const IFeatureVector& l_rFeatureVector=rFeatureVectorSet[i];

		float64 l_f64Label=l_rFeatureVector.getLabel();

		if(l_f64Label==m_f64Class1)
		{
			l_oDiff=itpp::vec(l_rFeatureVector.getBuffer(), l_rFeatureVector.getSize()) - l_oMeanFeatureVector1;
			l_oSigma+=itpp::outer_product(l_oDiff, l_oDiff) / (l_ui32NumberOfFeatureVectors-2.);
		}

		if(l_f64Label==m_f64Class2)
		{
			l_oDiff=itpp::vec(l_rFeatureVector.getBuffer(), l_rFeatureVector.getSize()) - l_oMeanFeatureVector2;
			l_oSigma+=itpp::outer_product(l_oDiff, l_oDiff) / (l_ui32NumberOfFeatureVectors-2.);
		}
	}

	itpp::mat l_oSigmaInverse=itpp::inv(l_oSigma);

	m_oCoefficients=l_oSigmaInverse * (l_oMeanFeatureVector1 - l_oMeanFeatureVector2);
	m_oCoefficients.ins(0, -0.5 * ((l_oMeanFeatureVector1+l_oMeanFeatureVector2).transpose() * m_oCoefficients)[0]);

	return true;
}

boolean CAlgorithmClassifierLDA::classify(const IFeatureVector& rFeatureVector, float64& rf64Class, IVector& rClassificationValues)
{
	if(rFeatureVector.getSize()+1!=(unsigned int)m_oCoefficients.size())
	{
		this->getLogManager() << LogLevel_Warning << "Feature vector size " << rFeatureVector.getSize() << " and hyperplane parameter size " << (uint32) m_oCoefficients.size() << " does not match\n";
		return false;
	}

	itpp::vec l_oFeatures(rFeatureVector.getBuffer(), rFeatureVector.getSize());

	l_oFeatures.ins(0, 1);

	const float64 l_f64Result=-l_oFeatures*m_oCoefficients;

	rClassificationValues.setSize(1);
	rClassificationValues[0]=l_f64Result;

	if(l_f64Result < 0)
	{
		rf64Class=m_f64Class1;
	}
	else
	{
		rf64Class=m_f64Class2;
	}
	return true;
}

void CAlgorithmClassifierLDA::generateConfigurationNode(void)
{
	std::stringstream l_sClasses;
	std::stringstream l_sCoefficients;

	l_sClasses << m_f64Class1 << " " << m_f64Class2;
	l_sCoefficients << std::scientific << m_oCoefficients[0];
	for(int i=1; i<m_oCoefficients.size(); i++)
	{
		l_sCoefficients << " " << m_oCoefficients[i];
	}

	XML::IXMLNode *l_pClassesNode = XML::createNode(c_sClassesNodeName);
	l_pClassesNode->setPCData(l_sClasses.str().c_str());
	XML::IXMLNode *l_pCoefficientsNode = XML::createNode(c_sCoeffNodeName);
	l_pCoefficientsNode->setPCData(l_sCoefficients.str().c_str());

	XML::IXMLNode *l_pAlgorithmNode  = XML::createNode(c_sTypeNodeName);
	l_pAlgorithmNode->addChild(l_pClassesNode);
	l_pAlgorithmNode->addChild(l_pCoefficientsNode);

	m_pConfigurationNode = XML::createNode(c_sClassifierRoot);
	m_pConfigurationNode->addChild(l_pAlgorithmNode);
}

XML::IXMLNode* CAlgorithmClassifierLDA::saveConfiguration(void)
{
	this->generateConfigurationNode();
	return m_pConfigurationNode;
}

boolean CAlgorithmClassifierLDA::loadConfiguration(XML::IXMLNode *pConfiguratioNode)
{
	m_f64Class1=0;
	m_f64Class2=0;

	loadClassesFromNode(pConfiguratioNode->getChild(0)->getChildByName(c_sClassesNodeName));
	loadCoefficientsFromNode(pConfiguratioNode->getChild(0)->getChildByName(c_sCoeffNodeName));

	return true;
}

void CAlgorithmClassifierLDA::loadClassesFromNode(XML::IXMLNode *pNode)
{
	std::stringstream l_sData(pNode->getPCData());

	l_sData >> m_f64Class1;
	l_sData >> m_f64Class2;
}

void CAlgorithmClassifierLDA::loadCoefficientsFromNode(XML::IXMLNode *pNode)
{
	std::stringstream l_sData(pNode->getPCData());

	std::vector < float64 > l_vCoefficients;
	while(!l_sData.eof())
	{
		float64 l_f64Value;
		l_sData >> l_f64Value;
		l_vCoefficients.push_back(l_f64Value);
	}

	m_oCoefficients.set_size(l_vCoefficients.size());
	for(size_t i=0; i<l_vCoefficients.size(); i++)
	{
		m_oCoefficients[i]=l_vCoefficients[i];
	}
}

#endif // TARGET_HAS_ThirdPartyITPP
