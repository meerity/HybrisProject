/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import {
    IDecoratorService,
    IFeatureService,
    IPerspectiveService,
    moduleUtils,
    SeTranslationModule,
    SeEntryModule
} from 'smarteditcommons';
import { AbAnalyticsModule } from 'trainingsmarteditcommons';
import { AbAnalyticsDecoratorModule } from './abAnalyticsDecorator';

@SeEntryModule('trainingsmartedit')
@NgModule({
    imports: [
        BrowserModule,
        SeTranslationModule.forChild(),
        AbAnalyticsModule,
        AbAnalyticsDecoratorModule
    ],
    providers: [
        moduleUtils.bootstrap(
            (
                decoratorService: IDecoratorService,
                featureService: IFeatureService,
                perspectiveService: IPerspectiveService
            ) => {
                /// /////////////////////////////////////////////////
                // Create Decorator
                /// /////////////////////////////////////////////////

                // Use the decoratorService.addMappings() method to associate decorators
                // The keys may be given as strings or as regex
                decoratorService.addMappings({
                    SimpleResponsiveBannerComponent: ['abAnalyticsDecorator'],
                    CMSParagraphComponent: ['abAnalyticsDecorator']
                });

                // Register new decorators the the featureService
                // The key MUST be the same name as the component selector in camelCase
                featureService.addDecorator({
                    key: 'abAnalyticsDecorator',
                    nameI18nKey: 'trainingsmartedit.sample.decorator.name'
                });

                /// /////////////////////////////////////////////////
                // Create  Perspective and assign features.
                /// /////////////////////////////////////////////////
                // Group the features created above in a perspective. This will enable the features once the user selects this new perspective.
                perspectiveService.register({
                    key: 'abAnalyticsPerspective',
                    nameI18nKey: 'trainingsmartedit.sample.perspective.name',
                    descriptionI18nKey: 'trainingsmartedit.sample.perspective.description',
                    features: ['abAnalyticsToolbarItem', 'abAnalyticsDecorator'],
                    perspectives: []
                });
            },
            [IDecoratorService, IFeatureService, IPerspectiveService]
        )
    ]
})
export class TrainingsmarteditModule {}
